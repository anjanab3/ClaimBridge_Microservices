package com.cts.report.controller;

import com.cts.report.dto.ReportRequestDTO;
import com.cts.report.service.KPIService;
import com.cts.report.service.ReportingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reporting")
public class ReportingController {

    @Autowired
    private ReportingService reportingService;

    @Autowired
    private KPIService kpiService;

    /*
        REPORTS  —  Combined GET
        type = ALL_REPORTS                      → all reports
        type = REPORT_BY_ID   &reportId={id}    → report by ID
        type = REPORT_BY_SCOPE &scope={scope}   → OPERATIONAL / COMPLIANCE / FRAUD
    */
    @PreAuthorize("hasAuthority('AUDITOR') or hasAuthority('COMPLIANCE')")
    @GetMapping("/reports/query")
    public ResponseEntity<Object> queryReports(
            @RequestParam String type,
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) String scope,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        switch (type.toUpperCase()) {

            case "ALL_REPORTS" -> {
                return ResponseEntity.ok(reportingService.getAllReports(page, size));
            }
            case "REPORT_BY_ID" -> {
                if (reportId == null)
                    throw new IllegalArgumentException("reportId is required for type REPORT_BY_ID");
                return ResponseEntity.ok(reportingService.getReportById(reportId));
            }
            case "REPORT_BY_SCOPE" -> {
                if (scope == null || scope.isBlank())
                    throw new IllegalArgumentException("scope is required for type REPORT_BY_SCOPE. Allowed: OPERATIONAL, COMPLIANCE, FRAUD");
                return ResponseEntity.ok(reportingService.getReportsByScope(scope, page, size));
            }
            default -> throw new EntityNotFoundException(
                    "Invalid type. Allowed values: ALL_REPORTS, REPORT_BY_ID, REPORT_BY_SCOPE");
        }
    }

    // Generate a new report
    @PreAuthorize("hasAuthority('COMPLIANCE')")
    @PostMapping("/reports/generate")
    public ResponseEntity<Object> generateReport(@RequestBody ReportRequestDTO request) {
        if (request.getScope() == null || request.getScope().isBlank())
            throw new IllegalArgumentException("scope is required. Allowed values: OPERATIONAL, COMPLIANCE, FRAUD");
        return ResponseEntity.ok(reportingService.generateReport(request));
    }

    // Regulatory export — fetch reports by scope within a date range
    @PreAuthorize("hasAuthority('COMPLIANCE')")
    @GetMapping("/reports/export")
    public ResponseEntity<Object> exportRegulatoryReports(
            @RequestParam String scope,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(reportingService.exportRegulatoryReports(scope, from, to, page, size));
    }

    /*
        AUDIT LOGS  —  Combined GET
        type = ALL_LOGS                               → all audit logs
        type = LOGS_BY_USER     &userId={id}          → logs for a user
        type = LOGS_BY_RESOURCE &resource={resource}  → Claim, FraudAlert, etc.
        type = LOGS_BY_ACTION   &action={action}      → LOGIN, UPDATE, DELETE, etc.
        type = LOGS_BY_DATE     &from=... &to=...     → date range
    */
    @PreAuthorize("hasAuthority('AUDITOR')")
    @GetMapping("/audit/query")
    public ResponseEntity<Object> queryAuditLogs(
            @RequestParam String type,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        switch (type.toUpperCase()) {

            case "ALL_LOGS" -> {
                return ResponseEntity.ok(reportingService.getAllAuditLogs(page, size));
            }
            case "LOGS_BY_USER" -> {
                if (userId == null)
                    throw new IllegalArgumentException("userId is required for type LOGS_BY_USER");
                return ResponseEntity.ok(reportingService.getAuditLogsByUser(userId, page, size));
            }
            case "LOGS_BY_RESOURCE" -> {
                if (resource == null || resource.isBlank())
                    throw new IllegalArgumentException("resource is required for type LOGS_BY_RESOURCE. e.g., Claim, FraudAlert, Policy");
                return ResponseEntity.ok(reportingService.getAuditLogsByResource(resource, page, size));
            }
            case "LOGS_BY_ACTION" -> {
                if (action == null || action.isBlank())
                    throw new IllegalArgumentException("action is required for type LOGS_BY_ACTION. e.g., LOGIN, UPDATE, DELETE");
                return ResponseEntity.ok(reportingService.getAuditLogsByAction(action, page, size));
            }
            case "LOGS_BY_DATE" -> {
                if (from == null || to == null)
                    throw new IllegalArgumentException("Both 'from' and 'to' are required for type LOGS_BY_DATE");
                return ResponseEntity.ok(reportingService.getAuditLogsByDateRange(from, to, page, size));
            }
            default -> throw new EntityNotFoundException(
                    "Invalid type. Allowed values: ALL_LOGS, LOGS_BY_USER, LOGS_BY_RESOURCE, LOGS_BY_ACTION, LOGS_BY_DATE");
        }
    }

    // KPI — current values for all KPIs
    @PreAuthorize("hasAuthority('AUDITOR') or hasAuthority('UNDERWRITER')")
    @GetMapping("/kpi/current")
    public ResponseEntity<Object> getCurrentKPIs() {
        return ResponseEntity.ok(kpiService.getAllKPIs());
    }

    // Update a KPI target value
    @PreAuthorize("hasAuthority('AUDITOR')")
    @PatchMapping("/kpi/{kpiId}/target")
    public ResponseEntity<Object> updateKpiTarget(
            @PathVariable Long kpiId,
            @RequestParam java.math.BigDecimal target) {
        return ResponseEntity.ok(kpiService.updateTarget(kpiId, target));
    }
}
