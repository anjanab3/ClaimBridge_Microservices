package com.cts.report.service;

import com.cts.report.client.ClaimsServiceClient;
import com.cts.report.dto.SLABreachDTO;
import com.cts.report.dto.SLARequestDTO;
import com.cts.report.dto.SLAResponseDTO;
import com.cts.report.entity.SLA;
import com.cts.report.repository.SLARepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SLAService {

    @Autowired
    private SLARepository slaRepository;

    @Autowired
    private ClaimsServiceClient claimsServiceClient;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private KPIService kpiService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    // Seed default SLA rules on startup

    @PostConstruct
    public void seedSLAs() {
        if (slaRepository.count() > 0) return;

        slaRepository.saveAll(List.of(
                build("Claim Response SLA",       48,  168, "CLAIM"),
                build("Investigation SLA",        48,  240, "INVESTIGATION"),
                build("Fraud Alert SLA",          12,  72,  "FRAUD_ALERT"),
                build("Settlement SLA",           24,  120, "SETTLEMENT")
        ));
    }

    private SLA build(String name, int responseHours, int resolutionHours, String entity) {
        SLA sla = new SLA();
        sla.setName(name);
        sla.setResponseHours(responseHours);
        sla.setResolutionHours(resolutionHours);
        sla.setMonitoredEntity(entity);
        sla.setActive(true);
        return sla;
    }

    // Query SLAs

    public List<SLAResponseDTO> getAllSLAs() {
        return slaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<SLAResponseDTO> getSLAsByEntity(String entity) {
        return slaRepository.findByMonitoredEntityIgnoreCase(entity)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Create new SLA rules — used by admin REST endpoint

    public SLAResponseDTO createSLA(SLARequestDTO request) {
        validateRequest(request);
        SLA sla = new SLA();
        sla.setName(request.getName());
        sla.setResponseHours(request.getResponseHours());
        sla.setResolutionHours(request.getResolutionHours());
        sla.setMonitoredEntity(request.getMonitoredEntity().toUpperCase());
        sla.setActive(request.getActive() != null ? request.getActive() : true);
        return toDTO(slaRepository.save(sla));
    }

    // Update (includes toggling active)

    public SLAResponseDTO updateSLA(Long id, SLARequestDTO request) {
        SLA sla = slaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("SLA not found: " + id));
        if (request.getName() != null)             sla.setName(request.getName());
        if (request.getResponseHours() != null)    sla.setResponseHours(request.getResponseHours());
        if (request.getResolutionHours() != null)  sla.setResolutionHours(request.getResolutionHours());
        if (request.getMonitoredEntity() != null)  sla.setMonitoredEntity(request.getMonitoredEntity().toUpperCase());
        if (request.getActive() != null)           sla.setActive(request.getActive());
        return toDTO(slaRepository.save(sla));
    }

    // Breach Detection

    /*
        Core breach computation — used both by the REST endpoint and the scheduler.
        TIER 1 — Response Breach: Entity is still in its INITIAL state beyond responseHours.
            CLAIM         → still IN_COMING
            INVESTIGATION → still OPEN
            FRAUD_ALERT   → still OPEN
            SETTLEMENT    → still PENDING
        TIER 2 — Resolution Breach: Entity has NOT reached a terminal state within resolutionHours.
            CLAIM         → not SETTLED / REJECTED / CLOSED
            INVESTIGATION → not CLOSED
            FRAUD_ALERT   → not RESOLVED
            SETTLEMENT    → not APPROVED / REJECTED
     */
    public List<SLABreachDTO> computeBreaches() {
        List<SLABreachDTO> breaches = new ArrayList<>();
        List<SLA> activeSLAs = slaRepository.findByActiveTrue();

        for (SLA sla : activeSLAs) {
            try {
                List<Map<String, Object>> records = fetchOpenRecords(sla.getMonitoredEntity());
                for (Map<String, Object> record : records) {
                    LocalDateTime createdAt = parseCreatedAt(record);
                    if (createdAt == null) continue;

                    long elapsedHours = ChronoUnit.HOURS.between(createdAt, LocalDateTime.now());
                    Long entityId = getLong(record, resolveIdField(sla.getMonitoredEntity()));
                    String currentStatus = getString(record, "status");

                    // TIER 1 — Response breach
                    if (isInitialState(currentStatus, sla.getMonitoredEntity())
                            && elapsedHours > sla.getResponseHours()) {
                        breaches.add(SLABreachDTO.builder()
                                .slaName(sla.getName())
                                .monitoredEntity(sla.getMonitoredEntity())
                                .breachType("RESPONSE")
                                .entityId(entityId)
                                .claimId(getLong(record, "claimId"))
                                .elapsedHours(elapsedHours)
                                .limitHours(sla.getResponseHours())
                                .exceededBy(elapsedHours - sla.getResponseHours())
                                .build());
                    }

                    // TIER 2 — Resolution breach
                    if (!isTerminalState(currentStatus, sla.getMonitoredEntity())
                            && elapsedHours > sla.getResolutionHours()) {
                        breaches.add(SLABreachDTO.builder()
                                .slaName(sla.getName())
                                .monitoredEntity(sla.getMonitoredEntity())
                                .breachType("RESOLUTION")
                                .entityId(entityId)
                                .claimId(getLong(record, "claimId"))
                                .elapsedHours(elapsedHours)
                                .limitHours(sla.getResolutionHours())
                                .exceededBy(elapsedHours - sla.getResolutionHours())
                                .build());
                    }
                }
            } catch (Exception e) {
                // Log and continue — one failing SLA check should not stop others
                System.err.println("[SLAService] Failed to check SLA: " + sla.getName() + " — " + e.getMessage());
            }
        }
        return breaches;
    }

    // Scheduled every hour — detects breaches and fires notifications + KPI update
    @Scheduled(cron = "0 0 * * * *")
    public void detectAndNotify() {
        List<SLABreachDTO> breaches = computeBreaches();
        for (SLABreachDTO breach : breaches) {
            String category = resolveCategory(breach.getMonitoredEntity());
            String message = String.format(
                    "[SLA BREACH] %s — %s breach on %s #%d. Elapsed: %dh (limit: %dh, exceeded by: %dh)",
                    breach.getSlaName(), breach.getBreachType(),
                    breach.getMonitoredEntity(), breach.getEntityId(),
                    breach.getElapsedHours(), breach.getLimitHours(), breach.getExceededBy());

            // Notify system-level (userId = null means broadcast to auditors/compliance)
            notificationService.createNotification(null, breach.getClaimId(), message, category);

            // Increment SLA_BREACH_COUNT KPI
            kpiService.incrementKpi("SLA_BREACH_COUNT");
        }
    }

    // State classification helpers

    private boolean isInitialState(String status, String entity) {
        if (status == null) return false;
        return switch (entity.toUpperCase()) {
            case "CLAIM"         -> "IN_COMING".equalsIgnoreCase(status);
            case "INVESTIGATION" -> "OPEN".equalsIgnoreCase(status);
            case "FRAUD_ALERT"   -> "OPEN".equalsIgnoreCase(status);
            case "SETTLEMENT"    -> "PENDING".equalsIgnoreCase(status);
            default              -> false;
        };
    }

    private boolean isTerminalState(String status, String entity) {
        if (status == null) return false;
        return switch (entity.toUpperCase()) {
            case "CLAIM"         -> status.equalsIgnoreCase("SETTLED")
                                 || status.equalsIgnoreCase("REJECTED")
                                 || status.equalsIgnoreCase("CLOSED");
            case "INVESTIGATION" -> status.equalsIgnoreCase("CLOSED");
            case "FRAUD_ALERT"   -> status.equalsIgnoreCase("RESOLVED");
            case "SETTLEMENT"    -> status.equalsIgnoreCase("APPROVED")
                                 || status.equalsIgnoreCase("REJECTED");
            default              -> false;
        };
    }

    // FeignClient fetch helpers

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> fetchOpenRecords(String entity) {
        Object response;
        switch (entity.toUpperCase()) {
            case "CLAIM" -> response = claimsServiceClient.getClaimsByStatus("IN_COMING", 0, 500).getBody();
            case "INVESTIGATION" -> response = claimsServiceClient.getClaimsByStatus("IN_REVIEW", 0, 500).getBody();
            case "FRAUD_ALERT" -> response = claimsServiceClient.getFraudAlertsByStatus("OPEN", 0, 500).getBody();
            default -> { return List.of(); }
        }
        if (response == null) return List.of();
        Map<String, Object> page = objectMapper.convertValue(response, Map.class);
        Object content = page.get("content");
        if (!(content instanceof List)) return List.of();
        return (List<Map<String, Object>>) content;
    }

    private String resolveIdField(String entity) {
        return switch (entity.toUpperCase()) {
            case "CLAIM"         -> "claimId";
            case "INVESTIGATION" -> "investigationId";
            case "FRAUD_ALERT"   -> "alertId";
            case "SETTLEMENT"    -> "settlementId";
            default              -> "id";
        };
    }

    private String resolveCategory(String entity) {
        return switch (entity.toUpperCase()) {
            case "CLAIM"         -> "INTAKE";
            case "INVESTIGATION" -> "INVESTIGATION";
            case "FRAUD_ALERT"   -> "FRAUD";
            case "SETTLEMENT"    -> "PAYMENT";
            default              -> "INTAKE";
        };
    }

    // Parsing utilities

    private LocalDateTime parseCreatedAt(Map<String, Object> record) {
        try {
            Object raw = record.get("createdAt");
            if (raw == null) return null;
            // Jackson deserialises LocalDateTime as [year,month,day,hour,min,sec,nano] array
            if (raw instanceof List<?> arr) {
                List<Integer> parts = (List<Integer>) arr;
                return LocalDateTime.of(parts.get(0), parts.get(1), parts.get(2),
                        parts.size() > 3 ? parts.get(3) : 0,
                        parts.size() > 4 ? parts.get(4) : 0);
            }
            // or as an ISO string
            return LocalDateTime.parse(raw.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Long getLong(Map<String, Object> record, String key) {
        Object val = record.get(key);
        if (val == null) return null;
        return Long.valueOf(val.toString());
    }

    private String getString(Map<String, Object> record, String key) {
        Object val = record.get(key);
        return val == null ? null : val.toString();
    }

    // Validation for SLA creation/update requests

    private void validateRequest(SLARequestDTO req) {
        if (req.getName() == null || req.getName().isBlank())
            throw new IllegalArgumentException("SLA name is required");
        if (req.getResponseHours() == null || req.getResolutionHours() == null)
            throw new IllegalArgumentException("responseHours and resolutionHours are required");
        if (req.getResponseHours() >= req.getResolutionHours())
            throw new IllegalArgumentException("resolutionHours must be greater than responseHours");
        if (req.getMonitoredEntity() == null || req.getMonitoredEntity().isBlank())
            throw new IllegalArgumentException("monitoredEntity is required. Allowed: CLAIM, INVESTIGATION, FRAUD_ALERT, SETTLEMENT");
    }

    // Mapper from SLA entity to SLAResponseDTO

    private SLAResponseDTO toDTO(SLA s) {
        return SLAResponseDTO.builder()
                .slaId(s.getSlaId())
                .name(s.getName())
                .responseHours(s.getResponseHours())
                .resolutionHours(s.getResolutionHours())
                .monitoredEntity(s.getMonitoredEntity())
                .active(s.getActive())
                .build();
    }
}
