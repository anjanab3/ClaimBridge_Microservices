package com.cts.report.service;

import com.cts.report.dto.ReportRequestDTO;
import com.cts.report.dto.ReportResponseDTO;
import com.cts.report.dto.AuditLogDTO;
import com.cts.report.entity.AuditLog;
import com.cts.report.entity.Report;
import com.cts.report.repository.AuditLogRepository;
import com.cts.report.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportingService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    // Report Retrieval and Generation

    public Page<ReportResponseDTO> getAllReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("generatedAt").descending());
        return reportRepository.findAll(pageable).map(this::toReportDTO);
    }

    public ReportResponseDTO getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));
        return toReportDTO(report);
    }

    public Page<ReportResponseDTO> getReportsByScope(String scope, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("generatedAt").descending());
        return reportRepository.findByScopeIgnoreCase(scope, pageable).map(this::toReportDTO);
    }

    public ReportResponseDTO generateReport(ReportRequestDTO request) {
        String scope = request.getScope().toUpperCase();
        Map<String, Object> metrics = buildMetrics(scope, request.getParametersJSON());

        Report report = new Report();
        report.setScope(scope);
        report.setParametersJSON(request.getParametersJSON());
        report.setMetricsJSON(metrics);
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportUri("/reports/" + scope.toLowerCase() + "/" + System.currentTimeMillis());

        return toReportDTO(reportRepository.save(report));
    }

    public Page<ReportResponseDTO> exportRegulatoryReports(String scope, LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("generatedAt").descending());
        return reportRepository
                .findByScopeIgnoreCaseAndGeneratedAtBetween(scope, from, to, pageable)
                .map(this::toReportDTO);
    }

    // Audit Log Retrieval and Management

    public Page<AuditLogDTO> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return auditLogRepository.findAll(pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return auditLogRepository.findByUserId(userId, pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByResource(String resource, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return auditLogRepository.findByResourceIgnoreCase(resource, pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByAction(String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return auditLogRepository.findByActionIgnoreCase(action, pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return auditLogRepository.findByTimestampBetween(from, to, pageable).map(this::toAuditDTO);
    }

    public void saveAuditLog(Long userId, String resource, Long resourceId, String action, String details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setResource(resource);
        log.setResourceId(resourceId);
        log.setAction(action);
        log.setDetails(details);
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);
    }

    // Mapper Methods

    private ReportResponseDTO toReportDTO(Report r) {
        return ReportResponseDTO.builder()
                .reportId(r.getReportId())
                .scope(r.getScope())
                .parametersJSON(r.getParametersJSON())
                .metricsJSON(r.getMetricsJSON())
                .generatedAt(r.getGeneratedAt())
                .reportUri(r.getReportUri())
                .generatedBy(r.getGeneratedBy())
                .build();
    }

    private AuditLogDTO toAuditDTO(AuditLog a) {
        return AuditLogDTO.builder()
                .logId(a.getLogId())
                .userId(a.getUserId())
                .resource(a.getResource())
                .resourceId(a.getResourceId())
                .action(a.getAction())
                .details(a.getDetails())
                .timestamp(a.getTimestamp())
                .build();
    }

    private Map<String, Object> buildMetrics(String scope, Map<String, Object> params) {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("generatedAt", LocalDateTime.now().toString());
        metrics.put("scope", scope);
        switch (scope) {
            case "OPERATIONAL" -> {
                metrics.put("cycleTimeTarget", "7 days");
                metrics.put("backlogStatus", "computed from KPI service");
            }
            case "COMPLIANCE" -> {
                metrics.put("regulatoryPeriod", params != null ? params.getOrDefault("period", "MONTHLY") : "MONTHLY");
                metrics.put("auditTrailIncluded", true);
            }
            case "FRAUD" -> {
                metrics.put("fraudDetectionRate", "computed from KPI service");
                metrics.put("openAlerts", "computed from KPI service");
            }
        }
        return metrics;
    }
}
