package com.cts.payment.service;

import com.cts.payment.entity.Settlement;
import com.cts.payment.repository.SettlementRepository;
import com.cts.payment.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
public class SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${claims.service.url}")
    private String claimsServiceUrl;

    // Get all settlements — needed by frontend overview
    public List<Settlement> getAllSettlements() {
        return settlementRepository.findAll();
    }

    // Get all settlements for a claim — mirrors claims-service SettlementService
    public List<Settlement> getSettlementsByClaim(Long claimId) {
        return settlementRepository.findByClaimId(claimId);
    }

    // Update settlement status — on APPROVED, notify claims-service to mark claim as SETTLED
    public Optional<Settlement> updateStatus(Long settlementId, String newStatus) {
        return settlementRepository.findById(settlementId)
                .map(settlement -> {
                    settlement.setStatus(Status.valueOf(newStatus));
                    Settlement saved = settlementRepository.save(settlement);

                    // Inter-service call — same pattern as TriageService.notifyClaimStatusUpdate()
                    if (Status.APPROVED.name().equals(newStatus)) {
                        notifyClaimStatusUpdate(saved.getClaimId(), "SETTLED");
                    }

                    return saved;
                });
    }

    // Notify claims-service to update the claim status
    private void notifyClaimStatusUpdate(Long claimId, String status) {
        try {
            String url = claimsServiceUrl + "/api/claims/" + claimId + "/status";
            restTemplate.put(url, status);
        } catch (Exception e) {
            // Log but don't fail — claims-service may be temporarily unavailable
            System.err.println("Warning: could not update claim status for claimId=" + claimId + ": " + e.getMessage());
        }
    }
}
