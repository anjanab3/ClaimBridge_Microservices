package com.cts.claimbridge.controller;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.util.ClaimStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal endpoint called by identity-service after a triage decision is created.
 * Updates the claim's status (e.g. IN_COMING → IN_REVIEW).
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimStatusController {

    @Autowired
    private ClaimRepository claimRepository;

    @PutMapping("/{claimId}/status")
    public ResponseEntity<?> updateClaimStatus(@PathVariable Long claimId,
                                               @RequestBody String status) {
        Claim claim = claimRepository.findById(claimId)
                .orElse(null);
        if (claim == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Claim not found: " + claimId);

        try {
            claim.setStatus(ClaimStatus.valueOf(status.trim().replace("\"", "")));
            claimRepository.save(claim);
            return ResponseEntity.ok("Claim status updated to " + status);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid status: " + status);
        }
    }
}
