package com.cts.report.client.fallback;

import com.cts.report.client.PolicyServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class PolicyServiceClientFallback implements PolicyServiceClient {

    private ResponseEntity<Object> emptyPage() {
        return ResponseEntity.ok(Map.of(
                "content", Collections.emptyList(),
                "totalElements", 0,
                "totalPages", 0
        ));
    }

    @Override
    public ResponseEntity<Object> getAllPolicies(int page, int size) {
        log.warn("[CircuitBreaker] policy-service unavailable — returning empty policies list");
        return emptyPage();
    }

    @Override
    public ResponseEntity<Object> getPolicyById(Long policyId) {
        log.warn("[CircuitBreaker] policy-service unavailable — cannot fetch policy {}", policyId);
        return ResponseEntity.ok(Collections.emptyMap());
    }

    @Override
    public ResponseEntity<Object> updatePolicyCoverage(Long policyId, String coverageJSON) {
        log.error("[CircuitBreaker] policy-service unavailable — could not update coverage for policy {}", policyId);
        return ResponseEntity.ok(Collections.emptyMap());
    }

    @Override
    public ResponseEntity<Object> updatePolicyStatus(Long policyId, String status) {
        log.error("[CircuitBreaker] policy-service unavailable — could not update status for policy {}", policyId);
        return ResponseEntity.ok(Collections.emptyMap());
    }
}
