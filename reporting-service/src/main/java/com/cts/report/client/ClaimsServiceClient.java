package com.cts.report.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for claims-service internal reporting endpoints.
 * These endpoints require no JWT — safe for service-to-service calls.
 */
@FeignClient(name = "claims-service", url = "${feign.client.config.claims-service.url}")
public interface ClaimsServiceClient {

    // ── Claims ───────────────────────────────────────────────────────────────

    @GetMapping("/api/internal/reporting/claims")
    ResponseEntity<Object> getAllClaims(
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/internal/reporting/claims/status/{status}")
    ResponseEntity<Object> getClaimsByStatus(
            @PathVariable("status") String status,
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/internal/reporting/claims/loss-type/{lossType}")
    ResponseEntity<Object> getClaimsByLossType(
            @PathVariable("lossType") String lossType,
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/internal/reporting/claims/trend")
    ResponseEntity<Object> getClaimTrend();

    // ── Fraud Alerts ─────────────────────────────────────────────────────────

    @GetMapping("/api/internal/reporting/fraud-alerts")
    ResponseEntity<Object> getAllFraudAlerts(
            @RequestParam int page,
            @RequestParam int size);

    @GetMapping("/api/internal/reporting/fraud-alerts/status/{status}")
    ResponseEntity<Object> getFraudAlertsByStatus(
            @PathVariable("status") String status,
            @RequestParam int page,
            @RequestParam int size);

    // ── Policies (proxied via claims-service → policy-service) ───────────────
    // Policies live in policy-service — use PolicyServiceClient instead.
}
