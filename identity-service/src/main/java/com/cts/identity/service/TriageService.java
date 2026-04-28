package com.cts.identity.service;

import com.cts.identity.dto.TriageDecisionRequestDTO;
import com.cts.identity.dto.TriageDecisionResponseDTO;
import com.cts.identity.dto.TriageRuleRequestDTO;
import com.cts.identity.dto.TriageRuleResponseDTO;
import com.cts.identity.entity.TriageDecision;
import com.cts.identity.entity.TriageRule;
import com.cts.identity.repository.TriageDecisionRepository;
import com.cts.identity.repository.TriageRuleRepository;
import com.cts.identity.repository.UserRepository;
import com.cts.identity.util.TriageStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TriageService {

    @Autowired private TriageRuleRepository ruleRepository;
    @Autowired private TriageDecisionRepository decisionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RestTemplate restTemplate;

    @Value("${claims.service.url}")
    private String claimsServiceUrl;

    // ── Triage Rules ─────────────────────────────────────────────────────────

    public Page<TriageRuleResponseDTO> getAllRules(int page, int size) {
        return ruleRepository.findAll(PageRequest.of(page, size)).map(this::toRuleDTO);
    }

    public TriageRuleResponseDTO createRule(TriageRuleRequestDTO req) {
        TriageRule rule = new TriageRule();
        applyRuleFields(rule, req);
        return toRuleDTO(ruleRepository.save(rule));
    }

    public TriageRuleResponseDTO updateRule(Long ruleId, TriageRuleRequestDTO req) {
        TriageRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
        applyRuleFields(rule, req);
        return toRuleDTO(ruleRepository.save(rule));
    }

    public void deleteRule(Long ruleId) {
        if (!ruleRepository.existsById(ruleId))
            throw new RuntimeException("Rule not found: " + ruleId);
        ruleRepository.deleteById(ruleId);
    }

    public TriageRuleResponseDTO toggleRule(Long ruleId) {
        TriageRule rule = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found: " + ruleId));
        rule.setActive(!Boolean.TRUE.equals(rule.getActive()));
        return toRuleDTO(ruleRepository.save(rule));
    }

    // ── Triage Decisions ─────────────────────────────────────────────────────

    public TriageDecisionResponseDTO createDecision(TriageDecisionRequestDTO req) {
        TriageRule rule = ruleRepository.findById(req.getRuleId())
                .orElseThrow(() -> new RuntimeException("Rule not found: " + req.getRuleId()));

        if (!Boolean.TRUE.equals(rule.getActive()))
            throw new IllegalStateException("Rule " + req.getRuleId() + " is not active");

        // Validate assignee exists in identity-service
        if (req.getAssignedTo() != null && userRepository.findById(req.getAssignedTo()).isEmpty())
            throw new RuntimeException("No user found with userId: " + req.getAssignedTo());

        // Escalate any existing OPEN decision for this claim
        decisionRepository.findTopByClaimIdOrderByAssignedAtDesc(req.getClaimId())
                .ifPresent(existing -> {
                    if (existing.getStatus() == TriageStatus.OPEN) {
                        existing.setStatus(TriageStatus.ESCALATED);
                        decisionRepository.save(existing);
                    }
                });

        TriageDecision decision = new TriageDecision();
        decision.setClaimId(req.getClaimId());
        decision.setTriageRule(rule);
        decision.setPriority(rule.getPriority());
        decision.setAssignedQueue(rule.getAssignedQueue());
        decision.setAssignedTo(req.getAssignedTo());
        decision.setStatus(TriageStatus.OPEN);
        decision.setAssignedAt(LocalDateTime.now());

        TriageDecision saved = decisionRepository.save(decision);

        // Notify claims-service to update claim status to IN_REVIEW
        notifyClaimStatusUpdate(req.getClaimId(), "IN_REVIEW");

        return toDecisionDTO(saved, "Triage decision created successfully");
    }

    public List<TriageDecisionResponseDTO> getDecisionsByClaimId(Long claimId) {
        List<TriageDecision> decisions = decisionRepository.findByClaimId(claimId);
        if (decisions.isEmpty())
            throw new RuntimeException("No triage decisions found for claim: " + claimId);
        return decisions.stream().map(d -> toDecisionDTO(d, null)).collect(Collectors.toList());
    }

    public List<TriageDecisionResponseDTO> getDecisionsByAssignee(String assignedTo) {
        return decisionRepository.findByAssignedTo(assignedTo)
                .stream().map(d -> toDecisionDTO(d, null)).collect(Collectors.toList());
    }

    public TriageDecisionResponseDTO updateDecision(Long decisionId, TriageDecisionRequestDTO req) {
        TriageDecision decision = decisionRepository.findById(decisionId)
                .orElseThrow(() -> new RuntimeException("Decision not found: " + decisionId));

        if (req.getRuleId() != null) {
            TriageRule rule = ruleRepository.findById(req.getRuleId())
                    .orElseThrow(() -> new RuntimeException("Rule not found: " + req.getRuleId()));
            decision.setTriageRule(rule);
            decision.setPriority(rule.getPriority());
            decision.setAssignedQueue(rule.getAssignedQueue());
        }
        if (req.getAssignedTo() != null) {
            if (userRepository.findById(req.getAssignedTo()).isEmpty())
                throw new RuntimeException("No user found with userId: " + req.getAssignedTo());
            decision.setAssignedTo(req.getAssignedTo());
        }
        if (req.getStatus() != null) decision.setStatus(req.getStatus());
        decision.setAssignedAt(LocalDateTime.now());

        return toDecisionDTO(decisionRepository.save(decision), "Triage decision updated successfully");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void applyRuleFields(TriageRule rule, TriageRuleRequestDTO req) {
        if (req.getName()          != null) rule.setName(req.getName());
        if (req.getConditionsJSON()!= null) rule.setConditionsJSON(req.getConditionsJSON());
        if (req.getPriority()      != null) rule.setPriority(req.getPriority());
        if (req.getAssignedQueue() != null) rule.setAssignedQueue(req.getAssignedQueue());
        if (req.getActive()        != null) rule.setActive(req.getActive());
        if (req.getIsDefault()     != null) rule.setIsDefault(req.getIsDefault());
    }

    private void notifyClaimStatusUpdate(Long claimId, String status) {
        try {
            String url = claimsServiceUrl + "/api/claims/" + claimId + "/status";
            restTemplate.put(url, status);
        } catch (Exception e) {
            // Log but don't fail — claims-service may be temporarily unavailable
            System.err.println("Warning: could not update claim status for claimId=" + claimId + ": " + e.getMessage());
        }
    }

    private TriageRuleResponseDTO toRuleDTO(TriageRule r) {
        return TriageRuleResponseDTO.builder()
                .ruleId(r.getRuleId())
                .name(r.getName())
                .conditionsJSON(r.getConditionsJSON())
                .priority(r.getPriority())
                .assignedQueue(r.getAssignedQueue())
                .active(r.getActive())
                .isDefault(r.getIsDefault())
                .build();
    }

    private TriageDecisionResponseDTO toDecisionDTO(TriageDecision d, String message) {
        return TriageDecisionResponseDTO.builder()
                .decisionId(d.getDecisionId())
                .claimId(d.getClaimId())
                .ruleId(d.getTriageRule() != null ? d.getTriageRule().getRuleId() : null)
                .priority(d.getPriority())
                .assignedQueue(d.getAssignedQueue())
                .assignedTo(d.getAssignedTo())
                .status(d.getStatus())
                .assignedAt(d.getAssignedAt())
                .message(message)
                .build();
    }
}
