package com.cts.payment.client;

import com.cts.payment.dto.AuditEventDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "reporting-service", url = "${reporting.service.url:http://localhost:9095}")
public interface ReportingServiceClient {

    /** Record any payment / settlement audit event in the reporting service */
    @PostMapping("/api/internal/events/audit")
    void logAudit(@RequestBody AuditEventDTO event);
}
