package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.*;
import com.cts.claimbridge.util.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.cts.claimbridge.util.TriageStatus;
import com.cts.claimbridge.dto.FraudAnalystRequestDTO;
import com.cts.claimbridge.dto.FraudAnalystResponseDTO;

import jakarta.persistence.EntityNotFoundException;

import javax.swing.text.html.Option;

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
    private TriageRuleRepository ruleRepository;

    @Autowired
    private UserRepository userRepository;

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
                // Not fraud — close the alert and send claim to ADJUSTER
                alert.setStatus("RESOLVED");
                alertRepository.save(alert);
                routeToAdjuster(alert, null); // no specific adjuster on CLEAR
                return FraudAnalystResponseDTO.builder()
                        .alertId(alert.getAlertId())
                        .claimId(alert.getClaim().getClaimId())
                        .status("RESOLVED")
                        .reason("Claim cleared as non-fraudulent")
                        .message("Fraud alert resolved. Claim has been routed to the ADJUSTER queue.")
                        .build();
            }

            case "ESCALATE" -> {
                // Needs senior review — stays in FRAUD queue
                alert.setStatus("ESCALATED");
                alert.setEscalatedTo("FA-001");
                alert.setEscalatedAt(LocalDateTime.now());
                alertRepository.save(alert);
                return FraudAnalystResponseDTO.builder()
                        .alertId(alert.getAlertId())
                        .claimId(alert.getClaim().getClaimId())
                        .status("ESCALATED")
                        .reason("Requires senior fraud analyst review")
                        .message("Alert escalated to SENIOR_FRAUD_ANALYST.")
                        .build();
            }

            case "REJECT" -> {
                // Confirmed fraud — claim is denied, no further routing
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

    // Reassign to a specific ADJUSTER — fraud analyst needs adjuster input but fraud case is not closed
    public FraudAnalystResponseDTO reassignToAdjuster(Long alertId, FraudAnalystRequestDTO request) {
        FraudAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new EntityNotFoundException("Fraud Alert not found for ID: " + alertId));

        // Validate the provided adjuster role_code exists in user table
        String adjusterRoleCode = request.getAssignedTo();
        if (adjusterRoleCode != null && !adjusterRoleCode.isBlank()) {
            userRepository.findByRoleCode(adjusterRoleCode)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "No user found with role_code: " + adjusterRoleCode));
        }

        alert.setStatus("REASSIGNED");
        alertRepository.save(alert);

        routeToAdjuster(alert, adjusterRoleCode);

        return FraudAnalystResponseDTO.builder()
                .alertId(alert.getAlertId())
                .claimId(alert.getClaim().getClaimId())
                .status("REASSIGNED")
                .reason("Adjuster review required")
                .message("Claim has been reassigned to adjuster: " +
                        (adjusterRoleCode != null ? adjusterRoleCode : "ADJUSTER queue"))
                .build();
    }

//    // Notify the claimant
//    public String notifyClaimant(String message) {
//        return "Claimant notified: " + message;
//    }


    // Creates a new TriageDecision routing the claim to the ADJUSTER queue
    // adjusterRoleCode: role_code of the specific adjuster (e.g. CA-001), or null for generic queue
    private void routeToAdjuster(FraudAlert alert, String adjusterRoleCode) {
        TriageRule defaultRule = ruleRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new EntityNotFoundException(
                        "No default rule configured. Cannot route claim to ADJUSTER."));

        // Prevent duplicate — skip if claim is already in ADJUSTER queue
        boolean alreadyAssigned = decisionRepository
                .findTopByClaimIdOrderByAssignedAtDesc(alert.getClaim().getClaimId())
                .map(d -> "ADJUSTER".equalsIgnoreCase(d.getAssignedQueue()))
                .orElse(false);

        if (alreadyAssigned)
            throw new IllegalStateException("Claim ID " + alert.getClaim().getClaimId() + " is already in the ADJUSTER queue");

        TriageDecision decision = new TriageDecision();
        decision.setClaimId(alert.getClaim().getClaimId());
        decision.setRuleId(defaultRule.getRuleID());
        decision.setPriority(defaultRule.getPriority());
        decision.setAssignedQueue("ADJUSTER");       // queue name
        decision.setAssignedTo(adjusterRoleCode);    // specific adjuster role_code e.g. CA-001 (can be null)
        decision.setStatus(TriageStatus.OPEN);
        decision.setAssignedAt(LocalDateTime.now());
        decisionRepository.save(decision);
    }

}
