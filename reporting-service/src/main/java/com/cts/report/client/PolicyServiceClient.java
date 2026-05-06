package com.cts.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Feign client for policy-service endpoints used by underwriter views.
 */
@FeignClient(name = "policy-service", url = "${feign.client.config.policy-service.url}")
public interface PolicyServiceClient {

    @GetMapping("/api/policies")
    ResponseEntity<Object> getAllPolicies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/policies/{policyId}")
    ResponseEntity<Object> getPolicyById(@PathVariable("policyId") Long policyId);

    @PutMapping("/api/policies/{policyId}")
    ResponseEntity<Object> updatePolicyCoverage(
            @PathVariable("policyId") Long policyId,
            @RequestBody String coverageJSON);

    @PatchMapping("/api/policies/{policyId}/status")
    ResponseEntity<Object> updatePolicyStatus(
            @PathVariable("policyId") Long policyId,
            @RequestParam String status);
}
