package com.cts.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for claims-service (registered in Eureka as "claims").
 * The underwriter endpoints mirror what was formerly handled inside claims-service directly.
 * Ensure claims-service exposes these paths or adjust the mappings to match existing ones.
 */
@FeignClient(name = "claims", configuration = FeignClientConfig.class)
public interface ClaimsServiceClient {

    // Claims Endpoints

    @GetMapping("/api/claims/all")
    ResponseEntity<Object> getAllClaims(
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/claims/by-status")
    ResponseEntity<Object> getClaimsByStatus(
            @RequestParam String status,
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/claims/by-loss-type")
    ResponseEntity<Object> getClaimsByLossType(
            @RequestParam String lossType,
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/claims/trend")
    ResponseEntity<Object> getClaimTrend();

    // Fraud Alerts Endpoints

    @GetMapping("/api/fraud-alerts/all")
    ResponseEntity<Object> getAllFraudAlerts(
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/fraud-alerts/by-status")
    ResponseEntity<Object> getFraudAlertsByStatus(
            @RequestParam String status,
            @RequestParam int page,
            @RequestParam int size);

    // Policies Endpoints

    @GetMapping("/api/policies/all")
    ResponseEntity<Object> getAllPolicies(
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/policies/{policyId}")
    ResponseEntity<Object> getPolicyById(@PathVariable Long policyId);

    @PutMapping("/api/policies/{policyId}/coverage")
    ResponseEntity<Object> updatePolicyCoverage(
            @PathVariable Long policyId,
            @RequestBody String coverageJSON);

    @PatchMapping("/api/policies/{policyId}/status")
    ResponseEntity<Object> updatePolicyStatus(
            @PathVariable Long policyId,
            @RequestParam String status);
}
