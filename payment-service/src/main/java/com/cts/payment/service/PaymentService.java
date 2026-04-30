package com.cts.payment.service;

import com.cts.payment.client.ClaimsServiceClient;
import com.cts.payment.entity.Payment;
import com.cts.payment.repository.PaymentRepository;
import com.cts.payment.util.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ClaimsServiceClient claimsServiceClient;

    public Payment initiatePayment(Long settlementId, Payment payment) {
        if (paymentRepository.existsBySettlementId(settlementId)) {
            throw new IllegalStateException("Payment for settlement ID "
                    + settlementId + " has already been initiated.");
        }
        // Update existing pre-created payment record from receiveSettlement
        Payment existing = paymentRepository.findBySettlementId(settlementId)
                .orElse(payment);
        existing.setSettlementId(settlementId);
        existing.setPayee(payment.getPayee());
        existing.setAmount(payment.getAmount());
        existing.setMethod(payment.getMethod());
        existing.setScheduledDate(payment.getScheduledDate());
        existing.setReference(payment.getReference());
        existing.setStatus(PaymentStatus.INITIATED);
        return paymentRepository.save(existing);
    }

    public Optional<Payment> getPaymentBySettlement(Long settlementId) {
        return paymentRepository.findBySettlementId(settlementId);
    }

    @Transactional
    public Optional<Payment> issuePayment(Long paymentId) {
        paymentRepository.findById(paymentId).ifPresent(payment -> {
            notifyClaimStatusUpdate(payment.getClaimId(), "SETTLED");
        });

        return paymentRepository.findById(paymentId)
                .map(payment -> {
                    payment.setStatus(PaymentStatus.SETTLED);
                    payment.setPaidDate(LocalDate.now());
                    return paymentRepository.save(payment);
                });
    }

    public Page<Payment> getAllPayments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return paymentRepository.findAll(pageable);
    }

    public boolean isPaymentAlreadyInitiated(Long settlementId) {
        return paymentRepository.existsBySettlementId(settlementId);
    }

    private void notifyClaimStatusUpdate(Long claimId, String status) {
        try {
            claimsServiceClient.updateClaimStatus(claimId, status);
        } catch (Exception e) {
            System.err.println("Warning: could not update claim status for claimId="
                    + claimId + ": " + e.getMessage());
        }
    }
}