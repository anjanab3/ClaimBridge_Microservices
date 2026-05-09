package com.cts.claimbridge.service;

import com.cts.claimbridge.client.IdentityServiceClient;
import com.cts.claimbridge.dto.FraudAnalystRequestDTO;
import com.cts.claimbridge.dto.FraudAnalystResponseDTO;
import com.cts.claimbridge.dto.TriageRuleDTO;
import feign.FeignException;
import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.FraudAlertRepository;
import com.cts.claimbridge.repository.FraudScoreRepository;
import com.cts.claimbridge.repository.TriageDecisionRepository;
import com.cts.claimbridge.util.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cts.claimbridge.util.TriageStatus;

import jakarta.persistence.EntityNotFoundException;

@Service
public class FraudAnalystService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private FraudAlertRepository alertRepository;

    @Autowired
    private FraudScoreRepository scoreRepository;

    @Autowired
    private TriageDecisionRepository decisionRepository;

    @Autowired
    private IdentityServiceClient identityServiceClient;

    // Get active fraud alerts only (OPEN, IN_PROGRESS, ESCALATED) — paginated
    // Excludes closed cases: REASSIGNED, RESOLVED, FRAUD
    public Page<FraudAlert> getAllFraudAlerts(Pageable pageable) {
        return alertRepository.findByStatusIn(List.of("OPEN", "IN_PROGRESS", "ESCALATED"), pageable);
    }

    // Get alert by ID
    public FraudAlert getAlertById(Long alertId) {
        return alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Fraud Alert not found for ID: " + alertId));
    }

    // Get alert by Claim ID
    public FraudAlert getAlertByClaimId(Long claimId) {
        return alertRepository.findByClaim_ClaimId(claimId).stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Fraud Alert not found for Claim ID: " + claimId));
    }

    // Get Fraud Score by Claim ID
    public FraudScore getFraudScore(Long claimId) {
        return scoreRepository.findByClaim_ClaimId(claimId)
                .orElseThrow(() -> new EntityNotFoundException("Fraud Score not found for Claim ID: " + claimId));
    }

    // Update the status and/or reason of the fraud alert for a claim.
    // At least one of status or reason must be provided.
    public FraudAnalystResponseDTO updateAlert(Long claimId, FraudAnalystRequestDTO request) {
        FraudAlert alert = alertRepository.findByClaim_ClaimId(claimId).stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No fraud alert found for Claim ID: " + claimId));

        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            alert.setStatus(request.getStatus().toUpperCase());
        }
        if (request.getReason() != null && !request.getReason().isBlank()) {
            alert.setReason(request.getReason());
        }
        alertRepository.save(alert);

        return FraudAnalystResponseDTO.builder()
                .alertId(alert.getAlertId())
                .claimId(claimId)
                .status(alert.getStatus())
                .reason(alert.getReason())
                .message("Fraud alert updated for Claim ID: " + claimId)
                .build();
    }

    // Add notes to an investigation
    public String addNotes(String notes) {
        return "Notes added: " + notes;
    }

    // Take a decision on a fraud alert
    // CLEAR     → not fraud, route claim back to ADJUSTER for normal processing
    // ESCALATE  → needs senior review, stays in FRAUD queue
    // REJECT    → confirmed fraud, claim is denied
    public FraudAnalystResponseDTO takeDecision(Long alertId, FraudAnalystRequestDTO request) {
        FraudAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Fraud Alert not found for ID: " + alertId));

        switch (request.getDecision().toUpperCase()) {

            case "CLEAR" -> {
                alert.setStatus("RESOLVED");
                alertRepository.save(alert);
                routeToAdjuster(alert, null);
                return FraudAnalystResponseDTO.builder()
                        .alertId(alert.getAlertId())
                        .claimId(alert.getClaimId())
                        .status("RESOLVED")
                        .reason("Claim cleared as non-fraudulent")
                        .message("Fraud alert resolved. Claim has been routed to the ADJUSTER queue.")
                        .build();
            }

            case "ESCALATE" -> {
                alert.setStatus("ESCALATED");
                alert.setEscalatedTo("FA-001");
                alert.setEscalatedAt(LocalDateTime.now());
                alertRepository.save(alert);
                return FraudAnalystResponseDTO.builder()
                        .alertId(alert.getAlertId())
                        .claimId(alert.getClaimId())
                        .status("ESCALATED")
                        .reason("Requires senior fraud analyst review")
                        .message("Alert escalated to SENIOR_FRAUD_ANALYST.")
                        .build();
            }

            case "REJECT" -> {
                alert.setStatus("FRAUD");
                Optional<Claim> claimresponse = claimRepository.findById(alert.getClaimId());
                claimresponse.get().setStatus(ClaimStatus.REJECTED);
                claimRepository.save(claimresponse.get());
                alertRepository.save(alert);
                return FraudAnalystResponseDTO.builder()
                        .alertId(alert.getAlertId())
                        .claimId(alert.getClaimId())
                        .status("FRAUD")
                        .reason("Confirmed fraudulent claim")
                        .message("Claim has been rejected as fraudulent.")
                        .build();
            }

            default -> throw new IllegalArgumentException("Invalid decision: " + request.getDecision());
        }
    }

    // Reassign to a specific ADJUSTER
    public FraudAnalystResponseDTO reassignToAdjuster(Long alertId, FraudAnalystRequestDTO request) {
        FraudAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Fraud Alert not found for ID: " + alertId));

        String adjusterRoleCode = request.getAssignedTo();

        alert.setStatus("REASSIGNED");
        alertRepository.save(alert);

        routeToAdjuster(alert, adjusterRoleCode);

        return FraudAnalystResponseDTO.builder()
                .alertId(alert.getAlertId())
                .claimId(alert.getClaimId())
                .status("REASSIGNED")
                .reason("Adjuster review required")
                .message("Claim has been reassigned to adjuster: " +
                        (adjusterRoleCode != null ? adjusterRoleCode : "ADJUSTER queue"))
                .build();
    }

    // Creates a new TriageDecision routing the claim to the ADJUSTER queue
    private void routeToAdjuster(FraudAlert alert, String adjusterRoleCode) {
        TriageRuleDTO defaultRule;
        try {
            defaultRule = identityServiceClient.getDefaultRule();
        } catch (FeignException e) {
            throw new EntityNotFoundException(
                "Cannot route claim to ADJUSTER: failed to fetch default triage rule " +
                "(identity-service returned " + e.status() + "). Ensure identity-service is running.");
        }

        boolean alreadyAssigned = decisionRepository
                .findTopByClaimIdOrderByAssignedAtDesc(alert.getClaimId())
                .map(d -> "ADJUSTER".equalsIgnoreCase(d.getAssignedQueue()))
                .orElse(false);

        if (alreadyAssigned)
            throw new IllegalStateException("Claim ID " + alert.getClaimId() + " is already in the ADJUSTER queue");

        TriageDecision decision = new TriageDecision();
        decision.setClaimId(alert.getClaimId());
        decision.setRuleId(defaultRule.getRuleId());
        decision.setPriority(defaultRule.getPriority());
        decision.setAssignedQueue("ADJUSTER");
        decision.setAssignedTo(adjusterRoleCode != null && !adjusterRoleCode.isBlank() ? adjusterRoleCode : "CA-0001");
        decision.setStatus(TriageStatus.OPEN);
        decision.setAssignedAt(LocalDateTime.now());
        decisionRepository.save(decision);
    }
}
