package com.cts.claimbridge.service;

import com.cts.claimbridge.client.IdentityServiceClient;
import com.cts.claimbridge.dto.TriageDecisionRequestDTO;
import com.cts.claimbridge.dto.TriageDecisionResponseDTO;
import com.cts.claimbridge.dto.TriageRuleDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.FraudAlert;
import com.cts.claimbridge.entity.TriageDecision;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.FraudAlertRepository;
import com.cts.claimbridge.repository.TriageDecisionRepository;
import com.cts.claimbridge.util.ClaimStatus;
import com.cts.claimbridge.util.Priority;
import com.cts.claimbridge.util.TriageStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TriageDecisionService {

    @Autowired private TriageDecisionRepository decisionRepository;
    @Autowired private IdentityServiceClient    identityServiceClient;
    @Autowired private ClaimRepository          claimRepository;
    @Autowired private FraudAlertRepository     fraudAlertRepository;

    // ── Create a triage decision ─────────────────────────────────────────────
    public TriageDecisionResponseDTO createDecision(TriageDecisionRequestDTO request) {

        if (!claimRepository.existsById(request.getClaimId()))
            throw new EntityNotFoundException("Claim not found for ID: " + request.getClaimId());

        // Update claim status to IN_REVIEW
        Optional<Claim> claim = claimRepository.findById(request.getClaimId());
        claim.get().setStatus(ClaimStatus.IN_REVIEW);
        claimRepository.save(claim.get());

        // Escalate any existing open decision
        Optional<TriageDecision> existing = decisionRepository.findTopByClaimIdOrderByAssignedAtDesc(request.getClaimId());
        if (existing.isPresent()) {
            existing.get().setStatus(TriageStatus.ESCALATED);
            decisionRepository.save(existing.get());
        }

        // Fetch rule from identity-service via Feign
        TriageRuleDTO rule;
        try {
            rule = identityServiceClient.getRuleById(request.getRuleId());
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("Triage Rule not found for ID: " + request.getRuleId());
        }
        if (Boolean.FALSE.equals(rule.getActive()))
            throw new IllegalStateException("Triage Rule ID " + request.getRuleId() + " is not active");

        // Prevent duplicate queue assignment
        decisionRepository.findTopByClaimIdOrderByAssignedAtDesc(request.getClaimId())
                .ifPresent(latest -> {
                    if (latest.getAssignedQueue() != null &&
                            latest.getAssignedQueue().equalsIgnoreCase(rule.getAssignedQueue())) {
                        throw new IllegalStateException(
                                "Claim ID " + request.getClaimId() + " is already in the " + rule.getAssignedQueue() + " queue");
                    }
                });

        // Default assignee based on queue — CA-0001 for adjuster, FA-0001 for fraud
        String assignedTo = request.getAssignedTo();
        if (assignedTo == null || assignedTo.isBlank()) {
            assignedTo = "FRAUD".equalsIgnoreCase(rule.getAssignedQueue()) ? "FA-0001" : "CA-0001";
        }

        TriageDecision decision = new TriageDecision();
        decision.setClaimId(request.getClaimId());
        decision.setRuleId(request.getRuleId());
        decision.setPriority(rule.getPriority());
        decision.setAssignedQueue(rule.getAssignedQueue());
        decision.setAssignedTo(assignedTo);
        decision.setStatus(TriageStatus.OPEN);
        decision.setAssignedAt(LocalDateTime.now());

        TriageDecision saved = decisionRepository.save(decision);

        // Auto-create FraudAlert if routed to FRAUD queue
        if ("FRAUD".equalsIgnoreCase(saved.getAssignedQueue())) {
            boolean alertExists = !fraudAlertRepository.findByClaim_ClaimId(saved.getClaimId()).isEmpty();
            if (!alertExists) {
                FraudAlert alert = new FraudAlert();
                claimRepository.findById(saved.getClaimId()).ifPresent(alert::setClaim);
                alert.setReason("Claim flagged and routed to FRAUD queue by intake agent");
                alert.setAssignedTo(saved.getAssignedTo());
                alert.setStatus("OPEN");
                fraudAlertRepository.save(alert);
            }
        }

        return mapToResponseDTO(saved, "Triage decision created and claim assigned successfully");
    }

    // ── Suggest the best matching rule for a claim ───────────────────────────
    public TriageRuleDTO suggestRuleForClaim(Long claimId) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Claim not found for ID: " + claimId));

        // Get all active rules from identity-service via Feign
        List<TriageRuleDTO> activeRules = identityServiceClient.getActiveRules()
                .stream()
                .filter(r -> !Boolean.TRUE.equals(r.getIsDefault()))
                .collect(Collectors.toList());

        ObjectMapper om = new ObjectMapper();

        for (TriageRuleDTO rule : activeRules) {
            try {
                String json = rule.getConditionsJSON();
                if (json == null || json.isBlank()) continue;

                @SuppressWarnings("unchecked")
                Map<String, Object> conditions = om.readValue(json, Map.class);
                if (conditions.isEmpty()) continue;

                // Match lossType
                String requiredType = (String) conditions.get("lossType");
                if (requiredType != null && !requiredType.isBlank()
                        && !requiredType.equalsIgnoreCase(claim.getLossType())) continue;

                // Match amount range (amountMin / amountMax)
                double amount = claim.getEstimatedAmount() != null ? claim.getEstimatedAmount() : 0.0;
                Object minObj = conditions.get("amountMin");
                Object maxObj = conditions.get("amountMax");
                if (minObj != null && amount < ((Number) minObj).doubleValue()) continue;
                if (maxObj != null && amount > ((Number) maxObj).doubleValue()) continue;

                // Match amount with operator string e.g. ">=10000"
                String amountCond = (String) conditions.get("amount");
                if (amountCond != null && !amountCond.isBlank()) {
                    amountCond = amountCond.trim();
                    if      (amountCond.startsWith(">=") && amount <  Double.parseDouble(amountCond.substring(2))) continue;
                    else if (amountCond.startsWith("<=") && amount >  Double.parseDouble(amountCond.substring(2))) continue;
                    else if (amountCond.startsWith(">")  && amount <= Double.parseDouble(amountCond.substring(1))) continue;
                    else if (amountCond.startsWith("<")  && amount >= Double.parseDouble(amountCond.substring(1))) continue;
                    else if (amountCond.startsWith("=")  && amount != Double.parseDouble(amountCond.substring(1))) continue;
                }

                return rule; // first match wins
            } catch (Exception ignored) {
                // Malformed conditionsJSON — skip
            }
        }

        // Fallback: default rule from identity-service
        try {
            return identityServiceClient.getDefaultRule();
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException(
                    "No matching rule found and no default rule is configured. Please contact an admin.");
        }
    }

    // ── Get all decisions for a claim ────────────────────────────────────────
    public List<TriageDecisionResponseDTO> getDecisionsByClaimId(Long claimId) {
        List<TriageDecision> decisions = decisionRepository.findByClaim_ClaimId(claimId);
        if (decisions.isEmpty())
            throw new EntityNotFoundException("No triage decisions found for Claim ID: " + claimId);
        return decisions.stream().map(d -> mapToResponseDTO(d, null)).collect(Collectors.toList());
    }

    // ── Get latest decision for a claim ──────────────────────────────────────
    public TriageDecisionResponseDTO getLatestDecision(Long claimId) {
        return decisionRepository.findTopByClaimIdOrderByAssignedAtDesc(claimId)
                .map(d -> mapToResponseDTO(d, null))
                .orElseThrow(() -> new EntityNotFoundException("No triage decision found for Claim ID: " + claimId));
    }

    // ── Get paginated decisions by queue ─────────────────────────────────────
    public Page<TriageDecisionResponseDTO> getDecisionsByAssignee(String assignedQueue, Pageable pageable) {
        return decisionRepository.findLatestByAssignedQueue(assignedQueue, pageable)
                .map(d -> mapToResponseDTO(d, null));
    }

    // ── Get decisions by priority ─────────────────────────────────────────────
    public List<TriageDecisionResponseDTO> getDecisionsByPriority(String priority) {
        Priority priorityEnum = Priority.valueOf(priority.toUpperCase());
        List<TriageDecision> decisions = decisionRepository.findByPriority(priorityEnum);
        if (decisions.isEmpty())
            throw new EntityNotFoundException("No triage decisions found for priority: " + priority);
        return decisions.stream().map(d -> mapToResponseDTO(d, null)).collect(Collectors.toList());
    }

    // ── Update an existing decision ───────────────────────────────────────────
    public TriageDecisionResponseDTO updateDecision(Long decisionId, TriageDecisionRequestDTO request) {
        TriageDecision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new EntityNotFoundException("Triage Decision not found for ID: " + decisionId));

        Long ruleIdToUse = request.getRuleId() != null ? request.getRuleId() : decision.getRuleId();

        // Fetch updated rule from identity-service
        TriageRuleDTO rule;
        try {
            rule = identityServiceClient.getRuleById(ruleIdToUse);
        } catch (FeignException.NotFound e) {
            throw new EntityNotFoundException("Triage Rule not found for ID: " + ruleIdToUse);
        }
        if (Boolean.FALSE.equals(rule.getActive()))
            throw new IllegalStateException("Triage Rule ID " + ruleIdToUse + " is not active");

        decision.setRuleId(ruleIdToUse);
        decision.setPriority(rule.getPriority());
        decision.setAssignedQueue(rule.getAssignedQueue());

        if (request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            decision.setAssignedTo(request.getAssignedTo());
        } else if (decision.getAssignedTo() == null || decision.getAssignedTo().isBlank()) {
            decision.setAssignedTo("FRAUD".equalsIgnoreCase(decision.getAssignedQueue()) ? "FA-0001" : "CA-0001");
        }
        if (request.getStatus() != null) {
            decision.setStatus(request.getStatus());
        }
        decision.setAssignedAt(LocalDateTime.now());
        TriageDecision updated = decisionRepository.save(decision);

        return mapToResponseDTO(updated, "Triage decision updated successfully");
    }

    // ── Private mapper ────────────────────────────────────────────────────────
    private TriageDecisionResponseDTO mapToResponseDTO(TriageDecision decision, String message) {
        return TriageDecisionResponseDTO.builder()
                .decisionId(decision.getDecisionId())
                .claimId(decision.getClaimId())
                .ruleId(decision.getRuleId())
                .priority(decision.getPriority() != null ? decision.getPriority() : null)
                .assignedQueue(decision.getAssignedQueue())
                .assignedTo(decision.getAssignedTo())
                .status(decision.getStatus() != null ? decision.getStatus() : null)
                .assignedAt(decision.getAssignedAt())
                .message(message)
                .build();
    }
}
