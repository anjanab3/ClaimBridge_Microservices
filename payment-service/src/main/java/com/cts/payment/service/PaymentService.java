package com.cts.payment.service;

import com.cts.payment.client.ClaimsServiceClient;
import com.cts.payment.client.ReportingServiceClient;
import com.cts.payment.dto.AuditEventDTO;
import com.cts.payment.entity.Payment;
import com.cts.payment.repository.PaymentRepository;
import com.cts.payment.repository.SettlementRepository;
import com.cts.payment.util.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private SettlementRepository settlementRepository;
    @Autowired
    private ClaimsServiceClient claimsServiceClient;
    @Autowired
    private ReportingServiceClient reportingServiceClient;

    public Payment initiatePayment(Long settlementId, Payment payment) {
        // Validate scheduled date — must not be null or in the past
        if (payment.getScheduledDate() == null) {
            throw new IllegalArgumentException("Scheduled date is required.");
        }
        if (!payment.getScheduledDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Scheduled date must be a future date. Please select tomorrow or later.");
        }

        // If a payment is already INITIATED, allow rescheduling by updating the date
        Optional<Payment> existing = paymentRepository.findBySettlementId(settlementId);
        if (existing.isPresent()) {
            Payment existingPayment = existing.get();
            if (existingPayment.getStatus() != PaymentStatus.INITIATED) {
                throw new IllegalStateException("Payment for settlement ID "
                        + settlementId + " has already been " + existingPayment.getStatus() + ".");
            }
            existingPayment.setScheduledDate(payment.getScheduledDate());
            Payment updated = paymentRepository.save(existingPayment);
            logAudit("Payment", updated.getPaymentId(), "PAYMENT_RESCHEDULED",
                    "Payment #" + updated.getPaymentId() + " rescheduled for Claim #"
                            + updated.getClaimId() + " (Settlement #" + settlementId
                            + ") by " + getActor());
            return updated;
        }

        // Resolve claimId from the Settlement table
        Long claimId = settlementRepository.findById(settlementId)
                .map(s -> s.getClaimId())
                .orElseThrow(() -> new RuntimeException(
                        "No settlement found for settlementId: " + settlementId));

        payment.setSettlementId(settlementId);
        payment.setClaimId(claimId);
        payment.setStatus(PaymentStatus.INITIATED);
        Payment saved = paymentRepository.save(payment);

        // Move claim to PAYMENT_SCHEDULED stage and notify the holder
        notifyClaimStatusUpdate(claimId, "PAYMENT_SCHEDULED");

        logAudit("Payment", saved.getPaymentId(), "PAYMENT_SCHEDULED",
                "Payment #" + saved.getPaymentId() + " scheduled for Claim #" + claimId
                        + " (Settlement #" + settlementId + ") by " + getActor());
        return saved;
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
                    Payment settled = paymentRepository.save(payment);
                    logAudit("Payment", settled.getPaymentId(), "PAYMENT_ISSUED",
                            "Payment #" + settled.getPaymentId() + " issued for Claim #"
                                    + settled.getClaimId() + " by " + getActor());
                    return settled;
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
}