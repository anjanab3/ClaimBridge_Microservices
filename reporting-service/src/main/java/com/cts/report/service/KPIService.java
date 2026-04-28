package com.cts.report.service;

import com.cts.report.dto.ClaimEventDTO;
import com.cts.report.dto.InvestigationEventDTO;
import com.cts.report.dto.KPIResponseDTO;
import com.cts.report.entity.KPI;
import com.cts.report.repository.KPIRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KPIService {

    @Autowired
    private KPIRepository kpiRepository;

    // Seed initial KPIs on startup if not present

    @PostConstruct
    public void seedKPIs() {
        if (kpiRepository.count() > 0) return;

        kpiRepository.saveAll(List.of(
                build("CLAIM_BACKLOG",
                        "Total open claims awaiting review",
                        BigDecimal.valueOf(50), "DAILY"),
                build("AVG_CYCLE_TIME_DAYS",
                        "Average days from claim submission to settlement",
                        BigDecimal.valueOf(7), "WEEKLY"),
                build("SETTLEMENT_RATE",
                        "Percentage of closed claims that are settled",
                        BigDecimal.valueOf(85), "MONTHLY"),
                build("FRAUD_DETECTION_RATE",
                        "Percentage of claims flagged as potential fraud",
                        BigDecimal.valueOf(5), "MONTHLY"),
                build("OPEN_FRAUD_ALERTS",
                        "Number of unresolved fraud alerts",
                        BigDecimal.valueOf(10), "DAILY"),
                build("OPEN_INVESTIGATIONS",
                        "Number of active investigations",
                        BigDecimal.valueOf(20), "DAILY"),
                build("AVG_INVESTIGATION_DURATION_DAYS",
                        "Average days investigations remain open",
                        BigDecimal.valueOf(5), "WEEKLY"),
                build("TOTAL_SETTLED_AMOUNT",
                        "Total settlement payout in the reporting period",
                        BigDecimal.valueOf(500000), "MONTHLY")
        ));
    }

    private KPI build(String name, String definition, BigDecimal target, String period) {
        KPI k = new KPI();
        k.setName(name);
        k.setDefinition(definition);
        k.setTarget(target);
        k.setCurrentValue(BigDecimal.ZERO);
        k.setReportingPeriod(period);
        return k;
    }

    // Event-driven incremental updates

    public void processClaimEvent(ClaimEventDTO event) {
        String prev = event.getPreviousStatus() == null ? "" : event.getPreviousStatus().toUpperCase();
        String next = event.getNewStatus() == null ? "" : event.getNewStatus().toUpperCase();

        // CLAIM_BACKLOG: open states add, terminal states subtract
        if (isOpenState(next) && !isOpenState(prev)) {
            adjustKpi("CLAIM_BACKLOG", BigDecimal.ONE);
        } else if (!isOpenState(next) && isOpenState(prev)) {
            adjustKpi("CLAIM_BACKLOG", BigDecimal.ONE.negate());
        }

        // TOTAL_SETTLED_AMOUNT: accumulate when settled
        if ("SETTLED".equals(next) && event.getEstimatedAmount() != null) {
            adjustKpi("TOTAL_SETTLED_AMOUNT", BigDecimal.valueOf(event.getEstimatedAmount()));
        }
    }

    public void processInvestigationEvent(InvestigationEventDTO event) {
        String prev = event.getPreviousStatus() == null ? "" : event.getPreviousStatus().toUpperCase();
        String next = event.getNewStatus() == null ? "" : event.getNewStatus().toUpperCase();

        if ("OPEN".equals(next) || "IN_PROGRESS".equals(next)) {
            if ("CLOSED".equals(prev)) adjustKpi("OPEN_INVESTIGATIONS", BigDecimal.ONE);
        }
        if ("CLOSED".equals(next) && !"CLOSED".equals(prev)) {
            adjustKpi("OPEN_INVESTIGATIONS", BigDecimal.ONE.negate());
        }
    }

    private boolean isOpenState(String status) {
        return "IN_COMING".equals(status) || "IN_REVIEW".equals(status);
    }

    private void adjustKpi(String name, BigDecimal delta) {
        kpiRepository.findByNameIgnoreCase(name).ifPresent(kpi -> {
            BigDecimal updated = (kpi.getCurrentValue() == null ? BigDecimal.ZERO : kpi.getCurrentValue()).add(delta);
            if (updated.compareTo(BigDecimal.ZERO) < 0) updated = BigDecimal.ZERO;
            kpi.setCurrentValue(updated);
            kpiRepository.save(kpi);
        });
    }

    // Scheduled nightly recalculation (authoritative reset)
    // Full recalculation via FeignClient is wired here when claims-service
    // exposes aggregate endpoints. Currently resets daily counters at midnight.

    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyKPIs() {
        List<KPI> daily = kpiRepository.findAll().stream()
                .filter(k -> "DAILY".equals(k.getReportingPeriod()))
                .collect(Collectors.toList());
        daily.forEach(k -> k.setCurrentValue(BigDecimal.ZERO));
        kpiRepository.saveAll(daily);
    }

    @Scheduled(cron = "0 0 0 * * MON")
    public void resetWeeklyKPIs() {
        List<KPI> weekly = kpiRepository.findAll().stream()
                .filter(k -> "WEEKLY".equals(k.getReportingPeriod()))
                .collect(Collectors.toList());
        weekly.forEach(k -> k.setCurrentValue(BigDecimal.ZERO));
        kpiRepository.saveAll(weekly);
    }

    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyKPIs() {
        List<KPI> monthly = kpiRepository.findAll().stream()
                .filter(k -> "MONTHLY".equals(k.getReportingPeriod()))
                .collect(Collectors.toList());
        monthly.forEach(k -> k.setCurrentValue(BigDecimal.ZERO));
        kpiRepository.saveAll(monthly);
    }

    // Query returns all KPIs with trend indicators (ON_TARGET, BELOW_TARGET)

    public List<KPIResponseDTO> getAllKPIs() {
        return kpiRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public KPIResponseDTO updateTarget(Long kpiId, BigDecimal newTarget) {
        KPI kpi = kpiRepository.findById(kpiId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("KPI not found: " + kpiId));
        kpi.setTarget(newTarget);
        return toDTO(kpiRepository.save(kpi));
    }

    private KPIResponseDTO toDTO(KPI k) {
        BigDecimal current = k.getCurrentValue() == null ? BigDecimal.ZERO : k.getCurrentValue();
        BigDecimal target  = k.getTarget() == null ? BigDecimal.ZERO : k.getTarget();
        String trend = current.compareTo(target) >= 0 ? "ON_TARGET" : "BELOW_TARGET";

        return KPIResponseDTO.builder()
                .kpiId(k.getKpiId())
                .name(k.getName())
                .definition(k.getDefinition())
                .target(k.getTarget())
                .currentValue(current)
                .reportingPeriod(k.getReportingPeriod())
                .trend(trend)
                .build();
    }
}
