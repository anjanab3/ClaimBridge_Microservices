package com.cts.payment.service;

import com.cts.payment.client.ClaimsServiceClient;
import com.cts.payment.client.ReportingServiceClient;
import com.cts.payment.dto.AuditEventDTO;
import com.cts.payment.dto.SettlementSyncDTO;
import com.cts.payment.entity.Settlement;
import com.cts.payment.repository.SettlementRepository;
import com.cts.payment.util.SettlementStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private ClaimsServiceClient claimsServiceClient;
    @Autowired
    private ReportingServiceClient reportingServiceClient;

    // Get all settlements — payout officer overview
    public List<Settlement> getAllSettlements() {
        return settlementRepository.findAll();
    }

    // Get settlement by claimId
    public Optional<Settlement> getSettlementsByClaim(Long claimId) {
        return settlementRepository.findByClaimId(claimId);
    }

    // Approve or reject — triggered by payout officer
    public void updateStatus(Long settlementId, String newStatus) {
        log.info("Updating settlement settlementId={} to status={}", settlementId, newStatus);
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException(
                        "No settlement found for settlementId: " + settlementId));

        String actor = getActor();

        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            settlement.setStatus(SettlementStatus.APPROVED);
            settlementRepository.save(settlement);
            log.info("Settlement {} approved by {}", settlementId, actor);
            logAudit("Settlement", settlementId, "SETTLEMENT_APPROVED",
                    "Settlement #" + settlementId + " for Claim #" + settlement.getClaimId()
                            + " approved by " + actor);
        } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
            settlement.setStatus(SettlementStatus.REJECTED);
            settlementRepository.save(settlement);
            log.info("Settlement {} rejected by {}", settlementId, actor);
            logAudit("Settlement", settlementId, "SETTLEMENT_REJECTED",
                    "Settlement #" + settlementId + " for Claim #" + settlement.getClaimId()
                            + " rejected by " + actor);
        } else {
            log.warn("Unknown status '{}' for settlementId={}", newStatus, settlementId);
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void logAudit(String resource, Long resourceId, String action, String details) {
        try {
            AuditEventDTO event = new AuditEventDTO();
            event.setResource(resource);
            event.setResourceId(resourceId);
            event.setAction(action);
            event.setDetails(details);
            reportingServiceClient.logAudit(event);
        } catch (Exception e) {
            log.warn("Could not write audit log for action={}: {}", action, e.getMessage());
        }
    }

    private String getActor() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    // Receives settlement pushed from claims-service via Feign — saves to settlement table
    public Settlement receiveSettlement(SettlementSyncDTO dto) {
        log.info("Receiving settlement for claimId={}", dto.getClaimId());
        // Avoid duplicates — one settlement per claim
        Optional<Settlement> existing = settlementRepository.findByClaimId(dto.getClaimId());
        if (existing.isPresent()) {
            log.info("Settlement already exists for claimId={}, returning existing", dto.getClaimId());
            return existing.get();
        }

        Settlement settlement = new Settlement();
        // Do NOT set settlementId — let the DB auto-generate via @GeneratedValue(IDENTITY)
        settlement.setClaimId(dto.getClaimId());
        settlement.setRecommendedAmount(dto.getRecommendedAmount());
        settlement.setRecommendedBy(dto.getRecommendedBy());
        settlement.setRecommendedAt(dto.getRecommendedAt());
        settlement.setStatus(SettlementStatus.PENDING);
        Settlement saved = settlementRepository.save(settlement);
        log.info("Settlement created with id={} for claimId={}", saved.getSettlementId(), dto.getClaimId());
        return saved;
    }
}