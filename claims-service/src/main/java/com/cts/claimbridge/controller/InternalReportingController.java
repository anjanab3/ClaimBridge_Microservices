package com.cts.claimbridge.controller;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.FraudAlert;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.FraudAlertRepository;
import com.cts.claimbridge.util.ClaimStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Internal service-to-service endpoints for reporting-service.
 * No JWT required — only callable from within the cluster.
 */
@RestController
@RequestMapping("/api/internal/reporting")
public class InternalReportingController {

    @Autowired private ClaimRepository claimRepository;
    @Autowired private FraudAlertRepository fraudAlertRepository;

    /** All claims paged — sorted by primary key (safe, always indexed) */
    @GetMapping("/claims")
    public ResponseEntity<Page<Claim>> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        try {
            return ResponseEntity.ok(
                claimRepository.findAll(PageRequest.of(page, size, Sort.by("claimId").descending()))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(new PageImpl<>(Collections.emptyList()));
        }
    }

    /** Claims filtered by status */
    @GetMapping("/claims/status/{status}")
    public ResponseEntity<Page<Claim>> getClaimsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        try {
            ClaimStatus cs = ClaimStatus.valueOf(status.toUpperCase());
            return ResponseEntity.ok(
                claimRepository.findByStatus(cs, PageRequest.of(page, size, Sort.by("claimId").descending()))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(new PageImpl<>(Collections.emptyList()));
        }
    }

    /** Claims filtered by loss type */
    @GetMapping("/claims/loss-type/{lossType}")
    public ResponseEntity<Page<Claim>> getClaimsByLossType(
            @PathVariable String lossType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        try {
            return ResponseEntity.ok(
                claimRepository.findByLossType(lossType, PageRequest.of(page, size, Sort.by("claimId").descending()))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(new PageImpl<>(Collections.emptyList()));
        }
    }

    /** Claim trend: count per lossType × status */
    @GetMapping("/claims/trend")
    public ResponseEntity<List<Map<String, Object>>> getClaimTrend() {
        try {
            List<Claim> all = claimRepository.findAll(Sort.by("claimId").descending());
            Map<String, Map<String, Long>> trend = all.stream()
                .collect(Collectors.groupingBy(
                    c -> c.getLossType() == null ? "OTHER" : c.getLossType().toUpperCase(),
                    Collectors.groupingBy(
                        c -> c.getStatus() == null ? "UNKNOWN" : c.getStatus().name(),
                        Collectors.counting()
                    )
                ));

            List<Map<String, Object>> result = trend.entrySet().stream()
                .map(e -> Map.of("lossType", (Object) e.getKey(), "counts", e.getValue()))
                .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }

    /** All fraud alerts paged — sorted by primary key */
    @GetMapping("/fraud-alerts")
    public ResponseEntity<Page<FraudAlert>> getAllFraudAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        try {
            return ResponseEntity.ok(
                fraudAlertRepository.findAll(PageRequest.of(page, size, Sort.by("alertId").descending()))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(new PageImpl<>(Collections.emptyList()));
        }
    }

    /** Fraud alerts filtered by status */
    @GetMapping("/fraud-alerts/status/{status}")
    public ResponseEntity<Page<FraudAlert>> getFraudAlertsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        try {
            return ResponseEntity.ok(
                fraudAlertRepository.findByStatus(status, PageRequest.of(page, size, Sort.by("alertId").descending()))
            );
        } catch (Exception e) {
            return ResponseEntity.ok(new PageImpl<>(Collections.emptyList()));
        }
    }
}
