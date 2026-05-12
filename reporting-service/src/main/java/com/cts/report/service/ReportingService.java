package com.cts.report.service;

import com.cts.report.dto.ReportRequestDTO;
import com.cts.report.dto.ReportResponseDTO;
import com.cts.report.dto.AuditLogDTO;
import com.cts.report.entity.AuditLog;
import com.cts.report.entity.Report;
import com.cts.report.repository.AuditLogRepository;
import com.cts.report.repository.KPIRepository;
import com.cts.report.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportingService {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private KPIRepository kpiRepository;

    // Report Retrieval and Generation

    public Page<ReportResponseDTO> getAllReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportId").descending());
        return reportRepository.findAll(pageable).map(this::toReportDTO);
    }

    public ReportResponseDTO getReportById(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new EntityNotFoundException("Report not found: " + reportId));
        return toReportDTO(report);
    }

    public Page<ReportResponseDTO> getReportsByScope(String scope, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportId").descending());
        return reportRepository.findByScopeIgnoreCase(scope, pageable).map(this::toReportDTO);
    }

    public ReportResponseDTO generateReport(ReportRequestDTO request) {
        String scope = request.getScope().toUpperCase();
        Map<String, Object> metrics = buildMetrics(scope, request.getParametersJSON());

        // Upsert — refresh the existing report for this scope instead of creating duplicates
        Report report = reportRepository
                .findTopByScopeIgnoreCaseOrderByGeneratedAtDesc(scope)
                .orElse(new Report());

        report.setScope(scope);
        report.setParametersJSON(request.getParametersJSON());
        report.setMetricsJSON(metrics);
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportUri("/reports/" + scope.toLowerCase() + "/" + System.currentTimeMillis());

        return toReportDTO(reportRepository.save(report));
    }

    public Page<ReportResponseDTO> exportRegulatoryReports(String scope, LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportId").descending());
        return reportRepository
                .findByScopeIgnoreCaseAndGeneratedAtBetween(scope, from, to, pageable)
                .map(this::toReportDTO);
    }

    // Audit Log Retrieval and Management

    public Page<AuditLogDTO> getAllAuditLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("logId").descending());
        return auditLogRepository.findAll(pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("logId").descending());
        return auditLogRepository.findByUserId(userId, pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByResource(String resource, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("logId").descending());
        return auditLogRepository.findByResourceIgnoreCase(resource, pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByAction(String action, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("logId").descending());
        return auditLogRepository.findByActionIgnoreCase(action, pageable).map(this::toAuditDTO);
    }

    public Page<AuditLogDTO> getAuditLogsByDateRange(LocalDateTime from, LocalDateTime to, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("logId").descending());
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
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("Report Generated At", LocalDateTime.now().toString());
        metrics.put("Scope", scope);

        switch (scope) {
            case "OPERATIONAL" -> {
                long totalEvents  = auditLogRepository.count();
                long claimCreates = auditLogRepository.countByResourceIgnoreCaseAndActionIgnoreCase("Claim", "CREATE");
                long statusChanges= auditLogRepository.countByResourceIgnoreCaseAndActionIgnoreCase("Claim", "STATUS_CHANGE");
                long triageEvents = auditLogRepository.countByResourceIgnoreCaseAndActionIgnoreCase("Claim", "TRIAGE");
                long invChanges   = auditLogRepository.countByResourceIgnoreCaseAndActionIgnoreCase("Investigation", "STATUS_CHANGE");

                metrics.put("Total Audit Events",               totalEvents);
                metrics.put("Claims Submitted",                 claimCreates);
                metrics.put("Claim Status Changes",             statusChanges);
                metrics.put("Triage Decisions Applied",         triageEvents);
                metrics.put("Investigation Status Changes",     invChanges);
                metrics.put("Claim Backlog (KPI)",              kpiValue("CLAIM_BACKLOG"));
                metrics.put("Avg Cycle Time — days (KPI)",      kpiValue("AVG_CYCLE_TIME_DAYS"));
                metrics.put("Open Investigations (KPI)",        kpiValue("OPEN_INVESTIGATIONS"));
                metrics.put("Avg Investigation Duration (KPI)", kpiValue("AVG_INVESTIGATION_DURATION_DAYS"));

                // Recent claim activity log
                metrics.put("recentActivity", recentLogs("Claim", 15));
            }
            case "COMPLIANCE" -> {
                long totalLogs     = auditLogRepository.count();
                long createActions = auditLogRepository.countByActionIgnoreCase("CREATE");
                long statusActions = auditLogRepository.countByActionIgnoreCase("STATUS_CHANGE");
                long triageActions = auditLogRepository.countByActionIgnoreCase("TRIAGE");
                long fraudLogs     = auditLogRepository.countByResourceIgnoreCase("FraudAlert");
                long paymentLogs   = auditLogRepository.countByResourceIgnoreCase("Payment");

                metrics.put("Total Audit Log Entries",          totalLogs);
                metrics.put("CREATE Actions",                   createActions);
                metrics.put("STATUS_CHANGE Actions",            statusActions);
                metrics.put("TRIAGE Actions",                   triageActions);
                metrics.put("Fraud Alert Audit Entries",        fraudLogs);
                metrics.put("Payment Audit Entries",            paymentLogs);
                metrics.put("Regulatory Period",                params != null ? params.getOrDefault("period", "MONTHLY") : "MONTHLY");
                metrics.put("Audit Trail Included",             true);
                metrics.put("Settlement Rate % (KPI)",          kpiValue("SETTLEMENT_RATE"));

                // Full recent audit trail
                metrics.put("recentActivity", recentLogsAll(20));
            }
            case "FRAUD" -> {
                long fraudCreates = auditLogRepository.countByResourceIgnoreCaseAndActionIgnoreCase("FraudAlert", "CREATE");
                long fraudChanges = auditLogRepository.countByResourceIgnoreCaseAndActionIgnoreCase("FraudAlert", "STATUS_CHANGE");

                metrics.put("Fraud Alerts Created",             fraudCreates);
                metrics.put("Fraud Alert Status Changes",       fraudChanges);
                metrics.put("Open Fraud Alerts (KPI)",          kpiValue("OPEN_FRAUD_ALERTS"));
                metrics.put("Fraud Detection Rate % (KPI)",     kpiValue("FRAUD_DETECTION_RATE"));

                // Recent fraud alert events
                metrics.put("recentActivity", recentLogs("FraudAlert", 15));
            }
            case "FINANCE" -> {
                long paymentScheduled = auditLogRepository.countByActionIgnoreCase("PAYMENT_SCHEDULED");
                long paymentIssued    = auditLogRepository.countByActionIgnoreCase("PAYMENT_ISSUED");
                long settlementApproved = auditLogRepository.countByActionIgnoreCase("SETTLEMENT_APPROVED");
                long settlementRejected = auditLogRepository.countByActionIgnoreCase("SETTLEMENT_REJECTED");

                metrics.put("Total Settled Amount (KPI)",       kpiValue("TOTAL_SETTLED_AMOUNT"));
                metrics.put("Settlement Rate % (KPI)",          kpiValue("SETTLEMENT_RATE"));
                metrics.put("Payments Scheduled",               paymentScheduled);
                metrics.put("Payments Issued",                  paymentIssued);
                metrics.put("Settlements Approved",             settlementApproved);
                metrics.put("Settlements Rejected",             settlementRejected);

                // Recent payment / settlement events
                metrics.put("recentActivity", recentLogs("Payment", 15));
            }
        }
        return metrics;
    }

    /** Returns the latest N audit log rows for a given resource as a list of maps. */
    private List<Map<String, Object>> recentLogs(String resource, int limit) {
        Pageable page = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        return auditLogRepository.findByResourceIgnoreCase(resource, page)
                .stream()
                .map(a -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("Log ID",     a.getLogId());
                    row.put("Action",     a.getAction());
                    row.put("Resource",   a.getResource());
                    row.put("Resource ID",a.getResourceId());
                    row.put("Details",    a.getDetails());
                    row.put("Timestamp",  a.getTimestamp() != null ? a.getTimestamp().toString() : "—");
                    return row;
                })
                .collect(Collectors.toList());
    }

    /** Returns the latest N audit log rows across all resources. */
    private List<Map<String, Object>> recentLogsAll(int limit) {
        Pageable page = PageRequest.of(0, limit, Sort.by("timestamp").descending());
        return auditLogRepository.findAll(page)
                .stream()
                .map(a -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("Log ID",     a.getLogId());
                    row.put("Action",     a.getAction());
                    row.put("Resource",   a.getResource());
                    row.put("Resource ID",a.getResourceId());
                    row.put("Details",    a.getDetails());
                    row.put("Timestamp",  a.getTimestamp() != null ? a.getTimestamp().toString() : "—");
                    return row;
                })
                .collect(Collectors.toList());
    }

    /** Returns the current KPI value as a plain number, or 0 if not found. */
    private BigDecimal kpiValue(String name) {
        return kpiRepository.findByNameIgnoreCase(name)
                .map(k -> k.getCurrentValue() != null ? k.getCurrentValue() : BigDecimal.ZERO)
                .orElse(BigDecimal.ZERO);
    }
}
