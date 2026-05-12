package com.cts.claimbridge.controller;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.service.NotificationService;
import com.cts.claimbridge.util.ClaimStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoint called by payment-service / triage after a status change.
 * Updates the claim's status and sends a holder notification when payment is scheduled.
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimStatusController {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private NotificationService notificationService;

    @PutMapping("/{claimId}/status")
    public ResponseEntity<?> updateClaimStatus(@PathVariable Long claimId,
                                               @RequestBody String status) {
        Claim claim = claimRepository.findById(claimId)
                .orElse(null);
        if (claim == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Claim not found: " + claimId);

        try {
            ClaimStatus newStatus = ClaimStatus.valueOf(status.trim().replace("\"", ""));
            claim.setStatus(newStatus);
            claimRepository.save(claim);

            // Notify the policyholder when payment has been scheduled
            if (newStatus == ClaimStatus.PAYMENT_SCHEDULED && claim.getHolderId() != null) {
                try {
                    notificationService.sendNotification(
                            claim.getHolderId(),
                            claimId,
                            "Your claim #" + claimId + " is now being processed — "
                                    + "a payment has been scheduled by our payout team.",
                            "PAYMENT");
                } catch (Exception ignored) { /* non-fatal */ }
            }

            return ResponseEntity.ok("Claim status updated to " + status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }
}
