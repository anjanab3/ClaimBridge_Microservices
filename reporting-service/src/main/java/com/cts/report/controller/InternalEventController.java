package com.cts.report.controller;

import com.cts.report.dto.ClaimEventDTO;
import com.cts.report.dto.InvestigationEventDTO;
import com.cts.report.service.KPIService;
import com.cts.report.service.ReportingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*
    Internal endpoints called by claims-service (and other microservices) whenever a claim status or investigation status changes.
    Permitted without JWT — secured by network boundary (internal traffic only).
 */
@RestController
@RequestMapping("/api/internal/events")
public class InternalEventController {

    @Autowired
    private KPIService kpiService;

    @Autowired
    private ReportingService reportingService;

    // Called by claims-service when a claim status changes
    @PostMapping("/claim-status-changed")
    public ResponseEntity<String> onClaimStatusChanged(@RequestBody ClaimEventDTO event) {
        // Update KPIs based on the transition
        kpiService.processClaimEvent(event);

        // Write an audit log entry
        reportingService.saveAuditLog(
                event.getUserId(),
                "Claim",
                event.getClaimId(),
                "STATUS_CHANGE",
                "Claim " + event.getClaimId() + " changed from " + event.getPreviousStatus() + " to " + event.getNewStatus()
        );

        return ResponseEntity.ok("Event processed");
    }

    // Called by claims-service when an investigation status changes
    @PostMapping("/investigation-changed")
    public ResponseEntity<String> onInvestigationChanged(@RequestBody InvestigationEventDTO event) {
        // Update KPIs
        kpiService.processInvestigationEvent(event);

        // Write an audit log entry
        reportingService.saveAuditLog(
                event.getUserId(),
                "Investigation",
                event.getInvestigationId(),
                "STATUS_CHANGE",
                "Investigation " + event.getInvestigationId() + " (Claim " + event.getClaimId() + ") changed from "
                        + event.getPreviousStatus() + " to " + event.getNewStatus()
        );

        return ResponseEntity.ok("Event processed");
    }
}
