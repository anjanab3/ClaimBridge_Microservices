package com.cts.payment.service;

import com.cts.payment.client.ClaimsServiceClient;
import com.cts.payment.dto.SettlementSyncDTO;
import com.cts.payment.entity.Payment;
import com.cts.payment.repository.PaymentRepository;
import com.cts.payment.util.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SettlementService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private ClaimsServiceClient claimsServiceClient;

    // Get all payments — payout officer overview
    public List<Payment> getAllSettlements() {
        return paymentRepository.findAll();
    }

    // Get payment by claimId
    public Optional<Payment> getSettlementsByClaim(Long claimId) {
        return paymentRepository.findByClaimId(claimId);
    }

    // Approve or reject — triggered by payout officer
    public void updateStatus(Long settlementId, String newStatus) {
        Payment payment = paymentRepository.findBySettlementId(settlementId)
                .orElseThrow(() -> new RuntimeException(
                        "No payment found for settlementId: " + settlementId));

        if ("APPROVED".equalsIgnoreCase(newStatus)) {
            payment.setStatus(PaymentStatus.INITIATED);
            paymentRepository.save(payment);
            notifyClaimStatusUpdate(payment.getClaimId(), "SETTLED");
        } else if ("REJECTED".equalsIgnoreCase(newStatus)) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
        }
    }

    // Receives settlement pushed from claims-service via Feign
    public Payment receiveSettlement(SettlementSyncDTO dto) {
        // Avoid duplicates
        if (paymentRepository.existsBySettlementId(dto.getSettlementId())) {
            return paymentRepository.findBySettlementId(dto.getSettlementId())
                    .orElseThrow(() -> new RuntimeException("Settlement already exists but could not be retrieved"));
        }

        Payment payment = new Payment();
        payment.setSettlementId(dto.getSettlementId());
        payment.setClaimId(dto.getClaimId());
        payment.setAmount(dto.getRecommendedAmount());
        payment.setPayee(dto.getRecommendedBy());
        payment.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void notifyClaimStatusUpdate(Long claimId, String status) {
        try {
            claimsServiceClient.updateClaimStatus(claimId, status);
        } catch (Exception e) {
            System.err.println("Warning: could not update claim status for claimId="
                    + claimId + ": " + e.getMessage());
        }
    }
}