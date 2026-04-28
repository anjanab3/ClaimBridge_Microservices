package com.cts.report.controller;

import com.cts.report.dto.ClaimTrendResponseDTO;
import com.cts.report.service.UnderwriterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAuthority('UNDERWRITER')")
@RequestMapping("/api/underwriter")
public class UnderwriterController {

    @Autowired
    private UnderwriterService underwriterService;

    /*
        Claims Query
        type = ALL_CLAIMS                           → all claims
        type = BY_STATUS    &status={status}        → filter by claim status (IN_COMING, IN_REVIEW, SETTLED, REJECTED, CLOSED)
        type = BY_LOSS_TYPE &lossType={type}        → filter by loss type (AUTO, PROPERTY, LIABILITY)
    */
    @GetMapping("/claims")
    public ResponseEntity<Object> queryClaims(
            @RequestParam String type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String lossType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        switch (type.toUpperCase()) {

            case "ALL_CLAIMS" -> {
                return ResponseEntity.ok(underwriterService.getAllClaims(page, size));
            }
            case "BY_STATUS" -> {
                if (status == null || status.isBlank())
                    throw new IllegalArgumentException("status is required for type BY_STATUS. e.g., IN_COMING, IN_REVIEW, SETTLED, REJECTED, CLOSED");
                return ResponseEntity.ok(underwriterService.getClaimsByStatus(status, page, size));
            }
            case "BY_LOSS_TYPE" -> {
                if (lossType == null || lossType.isBlank())
                    throw new IllegalArgumentException("lossType is required for type BY_LOSS_TYPE. e.g., AUTO, PROPERTY, LIABILITY");
                return ResponseEntity.ok(underwriterService.getClaimsByLossType(lossType, page, size));
            }
            default -> throw new IllegalArgumentException(
                    "Invalid type. Allowed values: ALL_CLAIMS, BY_STATUS, BY_LOSS_TYPE");
        }
    }

    // Claim trend summary — totals grouped by lossType and status
    @GetMapping("/claims/trend")
    public ResponseEntity<Object> getClaimTrend() {
        return ResponseEntity.ok(underwriterService.getClaimTrend());
    }

    /*
        Fraud Alerts Query
        type = ALL_ALERTS                           → all fraud alerts
        type = BY_STATUS    &status={status}        → filter by alert status (OPEN, ESCALATED, RESOLVED)
    */
    @GetMapping("/fraud-alerts")
    public ResponseEntity<Object> queryFraudAlerts(
            @RequestParam String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        switch (type.toUpperCase()) {

            case "ALL_ALERTS" -> {
                return ResponseEntity.ok(underwriterService.getAllFraudAlerts(page, size));
            }
            case "BY_STATUS" -> {
                if (status == null || status.isBlank())
                    throw new IllegalArgumentException("status is required for type BY_STATUS. e.g., OPEN, ESCALATED, RESOLVED");
                return ResponseEntity.ok(underwriterService.getFraudAlertsByStatus(status, page, size));
            }
            default -> throw new IllegalArgumentException(
                    "Invalid type. Allowed values: ALL_ALERTS, BY_STATUS");
        }
    }

    /*
        Policy Query
        type = ALL_POLICIES                         → all policies
        type = BY_ID        &policyId={id}          → single policy by ID
    */
    @GetMapping("/policies")
    public ResponseEntity<Object> queryPolicies(
            @RequestParam String type,
            @RequestParam(required = false) Long policyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        switch (type.toUpperCase()) {

            case "ALL_POLICIES" -> {
                return ResponseEntity.ok(underwriterService.getAllPolicies(page, size));
            }
            case "BY_ID" -> {
                if (policyId == null)
                    throw new IllegalArgumentException("policyId is required for type BY_ID");
                return ResponseEntity.ok(underwriterService.getPolicyById(policyId));
            }
            default -> throw new IllegalArgumentException(
                    "Invalid type. Allowed values: ALL_POLICIES, BY_ID");
        }
    }

    // Update policy coverage limits or endorsements
    @PutMapping("/policies/{policyId}/coverage")
    public ResponseEntity<Object> updatePolicyCoverage(
            @PathVariable Long policyId,
            @RequestBody String coverageJSON) {
        return ResponseEntity.ok(underwriterService.updatePolicyCoverage(policyId, coverageJSON));
    }

    // Update policy status (e.g., ACTIVE → SUSPENDED)
    @PatchMapping("/policies/{policyId}/status")
    public ResponseEntity<Object> updatePolicyStatus(
            @PathVariable Long policyId,
            @RequestParam String status) {
        return ResponseEntity.ok(underwriterService.updatePolicyStatus(policyId, status));
    }
}
