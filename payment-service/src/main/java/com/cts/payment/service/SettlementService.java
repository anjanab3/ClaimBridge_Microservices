package com.cts.payment.service;

import com.cts.payment.client.ClaimsServiceClient;
import com.cts.payment.dto.SettlementSyncDTO;
import com.cts.payment.entity.Settlement;
import com.cts.payment.repository.SettlementRepository;
import com.cts.payment.util.SettlementStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            settlement.setStatus(SettlementStatus.APPROVED);
            settlementRepository.save(settlement);
            log.info("Settlement {} approved", settlementId);
        } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
            settlement.setStatus(SettlementStatus.REJECTED);
            settlementRepository.save(settlement);
            log.info("Settlement {} rejected", settlementId);
        } else {
            log.warn("Unknown status '{}' for settlementId={}", newStatus, settlementId);
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