package com.cts.payment.controller;

import com.cts.payment.dto.ResponseDTO;
import com.cts.payment.dto.SettlementSyncDTO;
import com.cts.payment.entity.Payment;
import com.cts.payment.service.PaymentService;
import com.cts.payment.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private SettlementService settlementService;

    // Receive settlement from claims-service — internal Feign call, no auth needed
    @PostMapping("/receive")
    public ResponseEntity<?> receiveSettlement(@RequestBody SettlementSyncDTO dto) {
        try {
            Payment saved = settlementService.receiveSettlement(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO("Failed to receive settlement: " + e.getMessage()));
        }
    }

    // Approve or reject settlement — updates payment status
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @PostMapping("/{settlementId}/status")
    public ResponseEntity<?> updateSettlementStatus(
            @PathVariable("settlementId") Long settlementId,
            @RequestBody java.util.Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            settlementService.updateStatus(settlementId, newStatus);
            return ResponseEntity.ok(new ResponseDTO("Settlement status updated to " + newStatus));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(e.getMessage()));
        }
    }

    // Get all payments — payout officer overview
   // @PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @GetMapping
    public ResponseEntity<?> getAllPayments() {
        List<Payment> payments = settlementService.getAllSettlements();
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("No Payments Found"));
        }
        return ResponseEntity.ok(payments);
    }

    // Get payment by claimId
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<?> getPaymentByClaim(
            @PathVariable("claimId") Long claimId) {
        Optional<Payment> payment = settlementService.getSettlementsByClaim(claimId);
        if (payment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("No Payment Found for claimId: " + claimId));
        }
        return ResponseEntity.ok(payment.get());
    }

    // Initiate payment against a settlement
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @PostMapping("/{settlementId}/initiate")
    public ResponseEntity<?> initiatePayment(
            @PathVariable("settlementId") Long settlementId,
            @RequestBody Payment payment) {
        try {
            Payment saved = paymentService.initiatePayment(settlementId, payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    // Get payment by settlementId
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @GetMapping("/{settlementId}/payment")
    public ResponseEntity<?> getPaymentBySettlement(
            @PathVariable("settlementId") Long settlementId) {
        Optional<Payment> payment = paymentService.getPaymentBySettlement(settlementId);
        if (payment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("No Payment Found"));
        }
        return ResponseEntity.ok(payment.get());
    }

    // Issue payment — sets status SETTLED, notifies claims-service
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @PostMapping("/{paymentId}/issue")
    public ResponseEntity<?> issuePayment(
            @PathVariable("paymentId") Long paymentId) {
        return paymentService.issuePayment(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all payments paginated
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @GetMapping("/list")
    public ResponseEntity<?> getAllPaymentsPaginated(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "5") int size) {
        Page<Payment> payments = paymentService.getAllPayments(page, size);
        if (payments.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("No Payments Found"));
        }
        return ResponseEntity.ok(payments);
    }
}