package com.cts.report.client.fallback;

import com.cts.report.client.ClaimsServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
public class ClaimsServiceClientFallback implements ClaimsServiceClient {

    private ResponseEntity<Object> emptyPage() {
        return ResponseEntity.ok(Map.of(
                "content", Collections.emptyList(),
                "totalElements", 0,
                "totalPages", 0
        ));
    }

    @Override
    public ResponseEntity<Object> getAllClaims(int page, int size) {
        log.warn("[CircuitBreaker] claims-service unavailable — returning empty claims list");
        return emptyPage();
    }

    @Override
    public ResponseEntity<Object> getClaimsByStatus(String status, int page, int size) {
        log.warn("[CircuitBreaker] claims-service unavailable — returning empty claims for status {}", status);
        return emptyPage();
    }

    @Override
    public ResponseEntity<Object> getClaimsByLossType(String lossType, int page, int size) {
        log.warn("[CircuitBreaker] claims-service unavailable — returning empty claims for lossType {}", lossType);
        return emptyPage();
    }

    @Override
    public ResponseEntity<Object> getClaimTrend() {
        log.warn("[CircuitBreaker] claims-service unavailable — returning empty trend data");
        return ResponseEntity.ok(Collections.emptyMap());
    }

    @Override
    public ResponseEntity<Object> getAllFraudAlerts(int page, int size) {
        log.warn("[CircuitBreaker] claims-service unavailable — returning empty fraud alerts list");
        return emptyPage();
    }

    @Override
    public ResponseEntity<Object> getFraudAlertsByStatus(String status, int page, int size) {
        log.warn("[CircuitBreaker] claims-service unavailable — returning empty fraud alerts for status {}", status);
        return emptyPage();
    }
}
