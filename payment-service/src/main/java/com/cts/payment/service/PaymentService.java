package com.cts.payment.service;

import com.cts.payment.entity.Payment;
import com.cts.payment.entity.Settlement;
import com.cts.payment.repository.PaymentRepository;
import com.cts.payment.repository.SettlementRepository;
import com.cts.payment.util.PaymentStatus;
import com.cts.payment.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SettlementRepository settlementRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${claims.service.url}")
    private String claimsServiceUrl;

    public Payment initiatePayment(Long settlementId, Payment payment) {
        Optional<Payment> exists = paymentRepository.findBySettlementSettlementId(settlementId);
        Optional<Settlement> settlementResponse = settlementRepository.findById(settlementId);

        // Set settlement status to APPROVED when payment is initiated
        settlementResponse.get().setStatus(Status.APPROVED);
        settlementRepository.save(settlementResponse.get());

        if (exists.isPresent()) {
            throw new IllegalStateException("Payment for settlement ID " + settlementId + " has already been initiated.");
        }

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new RuntimeException("Settlement not found"));
        payment.setSettlement(settlement);
        payment.setStatus(PaymentStatus.valueOf("INITIATED"));
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentBySettlement(Long settlementId) {
        return paymentRepository.findBySettlement_SettlementId(settlementId);
    }

    @Transactional
    public Optional<Payment> issuePayment(Long paymentId) {
        // Update claim status to SETTLED via inter-service call (replaces claimRepository in monolith)
        paymentRepository.findById(paymentId).ifPresent(payment -> {
            Long claimId = payment.getSettlement().getClaimId();
            notifyClaimStatusUpdate(claimId, "SETTLED");
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
        return paymentRepository.existsById(settlementId);
    }

    // Inter-service call — same pattern as TriageService.notifyClaimStatusUpdate()
    private void notifyClaimStatusUpdate(Long claimId, String status) {
        try {
            String url = claimsServiceUrl + "/api/claims/" + claimId + "/status";
            restTemplate.put(url, status);
        } catch (Exception e) {
            System.err.println("Warning: could not update claim status for claimId=" + claimId + ": " + e.getMessage());
        }
    }
}
