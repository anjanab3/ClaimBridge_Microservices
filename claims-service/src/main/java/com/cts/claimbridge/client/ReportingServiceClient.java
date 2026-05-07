package com.cts.claimbridge.client;

import com.cts.claimbridge.dto.AuditEventDTO;
import com.cts.claimbridge.dto.ClaimEventDTO;
import com.cts.claimbridge.dto.InvestigationEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reporting-service")
public interface ReportingServiceClient {

    /** Fire when a claim status changes — also updates KPIs */
    @PostMapping("/api/internal/events/claim-status-changed")
    void onClaimStatusChanged(@RequestBody ClaimEventDTO event);

    /** Fire when an investigation status changes — also updates KPIs */
    @PostMapping("/api/internal/events/investigation-changed")
    void onInvestigationChanged(@RequestBody InvestigationEventDTO event);

    /** Fire for any other audit event (FraudAlert, Policy, etc.) */
    @PostMapping("/api/internal/events/audit")
    void logAudit(@RequestBody AuditEventDTO event);
}
