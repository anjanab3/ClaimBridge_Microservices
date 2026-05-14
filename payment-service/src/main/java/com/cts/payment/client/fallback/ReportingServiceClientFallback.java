package com.cts.payment.client.fallback;

import com.cts.payment.client.ReportingServiceClient;
import com.cts.payment.dto.AuditEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ReportingServiceClientFallback implements ReportingServiceClient {

    @Override
    public void logAudit(AuditEventDTO event) {
        log.warn("[CircuitBreaker] reporting-service unavailable — audit event dropped: action={}, entity={}",
                event != null ? event.getAction() : "unknown",
                event != null ? event.getResource() : "unknown");
    }
}
