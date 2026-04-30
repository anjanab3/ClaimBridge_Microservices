package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.TriageDecisionRequestDTO;
import com.cts.claimbridge.dto.TriageDecisionResponseDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.FraudAlert;
import com.cts.claimbridge.entity.TriageDecision;
import com.cts.claimbridge.entity.TriageRule;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.FraudAlertRepository;
import com.cts.claimbridge.repository.TriageDecisionRepository;
import com.cts.claimbridge.repository.TriageRuleRepository;
import com.cts.claimbridge.repository.UserRepository;
import com.cts.claimbridge.util.ClaimStatus;
import com.cts.claimbridge.util.Priority;
import com.cts.claimbridge.util.TriageStatus;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TriageDecisionService {

    @Autowired
    private TriageDecisionRepository decisionRepository;

    @Autowired
    private TriageRuleRepository ruleRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private FraudAlertRepository fraudAlertRepository;

    @Autowired
    private UserRepository userRepository;

    // Create a triage decision for a claim by applying a rule
    public TriageDecisionResponseDTO createDecision(TriageDecisionRequestDTO request) {

        // Validate claim exists
        if (!claimRepository.existsById(request.getClaimId()))
            throw new EntityNotFoundException("Claim not found for ID: " + request.getClaimId());

        // update claim status to IN_PROGRESS
        Optional<Claim> claim = claimRepository.findById(request.getClaimId());
        claim.get().setStatus(ClaimStatus.IN_REVIEW);
        claimRepository.save(claim.get());

        // update the triage decision as ESCALATED
        Optional<TriageDecision> triagedecision = decisionRepository.findTopByClaimIdOrderByAssignedAtDesc(request.getClaimId());
        if (triagedecision.isPresent()) {
            triagedecision.get().setStatus(TriageStatus.ESCALATED);
            decisionRepository.save(triagedecision.get());
        }

        // Validate rule exists and is active
        TriageRule rule = ruleRepository.findById(request.getRuleId())
                .orElseThrow(() -> new EntityNotFoundException("Triage Rule not found for ID: " + request.getRuleId()));
        if (Boolean.FALSE.equals(rule.getActive()))
            throw new IllegalStateException("Triage Rule ID " + request.getRuleId() + " is not active and cannot be applied");

        // Validate role_code if provided — must exist in user table
        if (request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            userRepository.findByRoleCode(request.getAssignedTo())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No user found with role_code: " + request.getAssignedTo()));
        }

        // Prevent duplicate — if claim is already in the same queue, reject
        decisionRepository.findTopByClaimIdOrderByAssignedAtDesc(request.getClaimId())
                .ifPresent(latest -> {
                    if (latest.getAssignedQueue() != null &&
                            latest.getAssignedQueue().equalsIgnoreCase(rule.getAssignedQueue())) {
                        throw new IllegalStateException(
                                "Claim ID " + request.getClaimId() + " is already in the " + rule.getAssignedQueue() + " queue");
                    }
                });

        TriageDecision decision = new TriageDecision();
        decision.setClaimId(request.getClaimId());
        decision.setRuleId(request.getRuleId());
        decision.setPriority(rule.getPriority());           // auto from rule
        decision.setAssignedQueue(rule.getAssignedQueue()); // auto from rule
        decision.setAssignedTo(request.getAssignedTo());    // role_code from request
        decision.setStatus(TriageStatus.OPEN);              // default on create
        decision.setAssignedAt(LocalDateTime.now());

        TriageDecision saved = decisionRepository.save(decision);

        // auto-store report on claim allocation
        Map<String, Object> allocationParams = new LinkedHashMap<>();
        allocationParams.put("claimId", saved.getClaimId());
        allocationParams.put("assignedQueue", saved.getAssignedQueue());
        allocationParams.put("assignedTo", saved.getAssignedTo());
        allocationParams.put("priority", saved.getPriority() != null ? saved.getPriority().name() : null);
        allocationParams.put("ruleId", saved.getRuleId());
        allocationParams.put("allocatedAt", saved.getAssignedAt().toString());
        //reportingService.recordEvent("CLAIM_ALLOCATION", allocationParams);

        // If routed to FRAUD queue — automatically create a FraudAlert (if one doesn't already exist)
        if ("FRAUD".equalsIgnoreCase(saved.getAssignedQueue())) {
            boolean alertExists = !fraudAlertRepository.findByClaim_ClaimId(saved.getClaimId()).isEmpty();
            if (!alertExists) {
                FraudAlert alert = new FraudAlert();
                Optional<Claim> claimresponse = claimRepository.findById(saved.getClaimId());
                alert.setClaim(claimresponse.get());
                alert.setReason("Claim flagged and routed to FRAUD queue by intake agent");
                alert.setStatus("OPEN");
                fraudAlertRepository.save(alert);
            }
        }

        return mapToResponseDTO(saved, "Triage decision created and claim assigned successfully");
    }

    // Get all decisions ever made for a claim
    public List<TriageDecisionResponseDTO> getDecisionsByClaimId(Long claimId) {
        List<TriageDecision> decisions = decisionRepository.findByClaim_ClaimId(claimId);
        if (decisions.isEmpty())
            throw new EntityNotFoundException("No triage decisions found for Claim ID: " + claimId);
        return decisions.stream().map(d -> mapToResponseDTO(d, null)).collect(Collectors.toList());
    }

    // Get the most recent triage decision for a claim
    public TriageDecisionResponseDTO getLatestDecision(Long claimId) {
        return decisionRepository.findTopByClaimIdOrderByAssignedAtDesc(claimId)
                .map(d -> mapToResponseDTO(d, null))
                .orElseThrow(() -> new EntityNotFoundException("No triage decision found for Claim ID: " + claimId));
    }

    // Get claims currently in a queue — only those whose LATEST decision is in that queue (paginated)
    public Page<TriageDecisionResponseDTO> getDecisionsByAssignee(String assignedQueue, Pageable pageable) {
        return decisionRepository.findLatestByAssignedQueue(assignedQueue, pageable)
                .map(d -> mapToResponseDTO(d, null));
    }

    // Get decisions filtered by priority
    public List<TriageDecisionResponseDTO> getDecisionsByPriority(String priority) {
        Priority priorityEnum = Priority.valueOf(priority.toUpperCase());
        List<TriageDecision> decisions = decisionRepository.findByPriority(priorityEnum);
        if (decisions.isEmpty())
            throw new EntityNotFoundException("No triage decisions found for priority: " + priority);
        return decisions.stream().map(d -> mapToResponseDTO(d, null)).collect(Collectors.toList());
    }

    // Update an existing decision — re-applies the rule's current priority and assignedQueue
    public TriageDecisionResponseDTO updateDecision(Long decisionId, TriageDecisionRequestDTO request) {
        TriageDecision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new EntityNotFoundException("Triage Decision not found for ID: " + decisionId));

        // If a new ruleId is provided, switch to that rule; otherwise keep the existing one
        Long ruleIdToUse = request.getRuleId() != null ? request.getRuleId() : decision.getRuleId();
        TriageRule rule = ruleRepository.findById(ruleIdToUse)
                .orElseThrow(() -> new EntityNotFoundException("Triage Rule not found for ID: " + ruleIdToUse));
        if (Boolean.FALSE.equals(rule.getActive()))
            throw new IllegalStateException("Triage Rule ID " + ruleIdToUse + " is not active and cannot be applied");

        decision.setRuleId(ruleIdToUse);
        decision.setPriority(rule.getPriority());              // auto from rule
        decision.setAssignedQueue(rule.getAssignedQueue());    // auto from rule
        // assigned_to (role_code) updated only if provided in request
        if (request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            userRepository.findByRoleCode(request.getAssignedTo())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No user found with role_code: " + request.getAssignedTo()));
            decision.setAssignedTo(request.getAssignedTo());
        }
        // status updated only if provided — manual transition
        if (request.getStatus() != null ) {
            decision.setStatus(request.getStatus());
        }
        decision.setAssignedAt(LocalDateTime.now());
        TriageDecision updated = decisionRepository.save(decision);

        // auto-store report on re-allocation
        Map<String, Object> reAllocParams = new LinkedHashMap<>();
        reAllocParams.put("claimId", updated.getClaimId());
        reAllocParams.put("assignedQueue", updated.getAssignedQueue());
        reAllocParams.put("assignedTo", updated.getAssignedTo());
        reAllocParams.put("priority", updated.getPriority() != null ? updated.getPriority().name() : null);
        reAllocParams.put("ruleId", updated.getRuleId());
        reAllocParams.put("allocatedAt", updated.getAssignedAt().toString());
        //reportingService.recordEvent("CLAIM_ALLOCATION", reAllocParams);

        return mapToResponseDTO(updated, "Triage decision updated successfully");
    }

    // Private mapper
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
