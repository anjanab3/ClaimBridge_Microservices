package com.cts.payment.controller;

import com.cts.payment.dto.ResponseDTO;
import com.cts.payment.entity.Payment;
import com.cts.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // Initiate payment against a settlement
    @PreAuthorize("hasAnyAuthority('PAYOUT_OFFICER')")
    @PostMapping("/{settlementId}/payment")
    public ResponseEntity<?> initiatePayment(@PathVariable Long settlementId, @RequestBody Payment payment) {
        if (paymentService.isPaymentAlreadyInitiated(settlementId)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Payment for settlement ID " + settlementId + " has already been initiated.");
        }
        Payment savedPayment = paymentService.initiatePayment(settlementId, payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPayment);
    }

    // Get payment by settlement
    @PreAuthorize("hasAnyAuthority('PAYOUT_OFFICER')")
    @GetMapping("/{settlementId}/payment")
    public ResponseEntity<?> getPaymentBySettlement(@PathVariable Long settlementId) {
        Optional<Payment> payment = paymentService.getPaymentBySettlement(settlementId);
        if (payment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Payment Found"));
        }
        return ResponseEntity.ok().body(payment.get());
    }

    // Issue payment — sets status SETTLED, paidDate = now, updates claim status in claims-service
    @PreAuthorize("hasAnyAuthority('PAYOUT_OFFICER')")
    @PostMapping("/{paymentId}/issue-payment")
    public ResponseEntity<?> issuePayment(@PathVariable Long paymentId) {
        return paymentService.issuePayment(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all payments paginated
    @PreAuthorize("hasAnyAuthority('PAYOUT_OFFICER')")
    @GetMapping("/paylist")
    public ResponseEntity<?> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<Payment> payments = paymentService.getAllPayments(page, size);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Payments Found"));
        }
        return ResponseEntity.ok().body(payments);
    }
}
