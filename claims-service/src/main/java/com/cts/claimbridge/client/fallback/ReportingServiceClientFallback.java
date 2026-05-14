package com.cts.claimbridge.client.fallback;

import com.cts.claimbridge.client.ReportingServiceClient;
import com.cts.claimbridge.dto.AuditEventDTO;
import com.cts.claimbridge.dto.ClaimEventDTO;
import com.cts.claimbridge.dto.InvestigationEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportingServiceClientFallback implements ReportingServiceClient {

    @Override
    public void onClaimStatusChanged(ClaimEventDTO event) {
        log.warn("[CircuitBreaker] reporting-service unavailable — claim status event dropped: {}", event);
    }

    @Override
    public void onInvestigationChanged(InvestigationEventDTO event) {
        log.warn("[CircuitBreaker] reporting-service unavailable — investigation event dropped: {}", event);
    }

    @Override
    public void logAudit(AuditEventDTO event) {
        log.warn("[CircuitBreaker] reporting-service unavailable — audit event dropped: action={}, entity={}",
                event != null ? event.getAction() : "unknown",
                event != null ? event.getResource() : "unknown");
    }

    @Override
    public void onEvidenceVerified(AuditEventDTO event) {
        log.warn("[CircuitBreaker] reporting-service unavailable — evidence-verified event dropped: {}", event);
    }
}
