package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.ClaimStatusDTO;
import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.dto.ValidationRequestDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.service.ClaimService;
import com.cts.claimbridge.util.ClaimStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
@CrossOrigin
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    // ── Submit a claim (query params) ──
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping
    public ResponseEntity<?> submitClaim(
            @RequestParam long holderId,
            @RequestParam long policyId,
            @RequestBody Claim claim) {
        try {
            return ResponseEntity.ok().body(
                new MessageDTO(
                    claimService.save(claim, policyId, holderId),
                    "Claim Created Successfully !!!"
                )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO(e.getMessage()));
        }
    }

    // ── Submit a claim (path variables) ──
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{holderId}/{policyId}")
    public ResponseEntity<?> submitClaimByPath(
            @PathVariable long holderId,
            @PathVariable long policyId,
            @RequestBody Claim claim) {
        try {
            return ResponseEntity.ok().body(
                new MessageDTO(
                    claimService.save(claim, policyId, holderId),
                    "Claim Created Successfully !!!"
                )
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO(e.getMessage()));
        }
    }

    // ── Get all claims — intake agent ──
    @PreAuthorize("hasAnyAuthority('CLAIMS_INTAKE_AGENT','ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Claim> allClaims = claimService.findAllClaims(page, size);
        if (allClaims.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("No Claims Found"));
        }
        return ResponseEntity.ok().body(allClaims);
    }

    // ── Get all claims for a holder ──        ← fixed URL to /holder/{holderId}
    //@PreAuthorize("hasAnyAuthority('USER','CLAIMS_INTAKE_AGENT','ADMIN')")
    @GetMapping("/holder/{holderId}")
    public ResponseEntity<?> getAllClaimsForHolder(@PathVariable Long holderId) {
        List<Claim> claims = claimService.findByHolder(holderId);
        if (claims.isEmpty()) {
            return ResponseEntity.ok()
                    .body(new ResponseDTO("No claims found"));
        }
        return ResponseEntity.ok().body(claims);
    }

    // ── Get incoming claims ──
    @PreAuthorize("hasAnyAuthority('CLAIMS_INTAKE_AGENT','ADMIN')")
    @GetMapping("/incoming")
    public ResponseEntity<?> getIncomingClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Claim> claims = claimService.getIncomingClaims(page, size);
        if (claims.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("No Incoming Claims Found"));
        }
        return ResponseEntity.ok().body(claims);
    }

    // ── Get a specific claim by claimId ──
    @PreAuthorize("hasAnyAuthority('USER','CLAIMS_INTAKE_AGENT','ADMIN')")
    @GetMapping("/{claimId}")
    public ResponseEntity<?> getClaim(@PathVariable Long claimId) {
        Optional<Claim> claim = claimService.findById(claimId);
        if (claim.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("Claim not found"));
        }
        return ResponseEntity.ok().body(claim.get());
    }

    // ── Validate claim coverage ──
    @PreAuthorize("hasAuthority('CLAIMS_INTAKE_AGENT')")
    @PutMapping("/{claimId}/validation")
    public ResponseEntity<?> validateCoverage(
            @PathVariable Long claimId,
            @RequestBody ValidationRequestDTO request) {
        try {
            Claim updated = claimService.validateClaim(
                claimId, ClaimStatus.valueOf(request.getStatus())
            );
            return ResponseEntity.ok().body(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO(e.getMessage()));
        }
    }

    // ── Get claim status ──
    @PreAuthorize("hasAnyAuthority('USER','CLAIMS_INTAKE_AGENT','ADMIN')")
    @GetMapping("/{claimId}/status")
    public ResponseEntity<ClaimStatusDTO> getClaimStatus(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.getClaimStatus(claimId));
    }

    // ── Filter by status ──
    @PreAuthorize("hasAnyAuthority('CLAIMS_INTAKE_AGENT','ADMIN','CLAIMS_ADJUSTER')")
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Claim> claims = claimService.findByStatus(
                ClaimStatus.valueOf(status.toUpperCase()), page, size
            );
            if (claims.isEmpty()) {
                return ResponseEntity.ok()
                        .body(new ResponseDTO("No claims found with status: " + status));
            }
            return ResponseEntity.ok().body(claims);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO("Invalid status: " + status));
        }
    }
}