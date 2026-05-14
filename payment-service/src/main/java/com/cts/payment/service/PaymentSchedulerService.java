package com.cts.payment.service;

import com.cts.payment.entity.Payment;
import com.cts.payment.repository.PaymentRepository;
import com.cts.payment.util.PaymentStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Runs every minute and auto-settles any INITIATED payment
 * whose scheduledDate has arrived or passed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentSchedulerService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    /**
     * Every 60 seconds: find all INITIATED payments where scheduledDate <= today
     * and automatically issue them → triggers SETTLED status on the claim.
     */
    @Scheduled(fixedDelay = 60000)
    public void processScheduledPayments() {
        List<Payment> duePayments = paymentRepository
                .findByStatusAndScheduledDateLessThanEqual(PaymentStatus.INITIATED, LocalDate.now());

        if (duePayments.isEmpty()) {
            return;
        }

        log.info("[Scheduler] Found {} payment(s) due for auto-settlement.", duePayments.size());

        for (Payment payment : duePayments) {
            try {
                paymentService.issuePayment(payment.getPaymentId());
                log.info("[Scheduler] Payment #{} for Claim #{} auto-settled successfully.",
                        payment.getPaymentId(), payment.getClaimId());
            } catch (Exception e) {
                log.error("[Scheduler] Failed to auto-settle Payment #{} for Claim #{}: {}",
                        payment.getPaymentId(), payment.getClaimId(), e.getMessage());
            }
        }
    }
}
