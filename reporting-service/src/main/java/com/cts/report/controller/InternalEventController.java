package com.cts.report.controller;

import com.cts.report.dto.ClaimEventDTO;
import com.cts.report.dto.InvestigationEventDTO;
import com.cts.report.service.KPIService;
import com.cts.report.service.NotificationService;
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

    @Autowired
    private NotificationService notificationService;

    // Called by claims-service when a claim status changes
    @PostMapping("/claim-status-changed")
    public ResponseEntity<String> onClaimStatusChanged(@RequestBody ClaimEventDTO event) {

        // 1. Update KPIs
        kpiService.processClaimEvent(event);

        // 2. Write audit log
        reportingService.saveAuditLog(
                event.getUserId(), "Claim", event.getClaimId(), "STATUS_CHANGE",
                "Claim " + event.getClaimId() + " changed from "
                        + event.getPreviousStatus() + " to " + event.getNewStatus());

        // 3. Send in-app notification to the affected user
        if (event.getUserId() != null) {
            String message = buildClaimNotification(event);
            notificationService.createNotification(
                    event.getUserId(), event.getClaimId(), message, resolveClaimCategory(event.getNewStatus()));
        }

        return ResponseEntity.ok("Event processed");
    }

    // Called by claims-service when an investigation status changes
    @PostMapping("/investigation-changed")
    public ResponseEntity<String> onInvestigationChanged(@RequestBody InvestigationEventDTO event) {

        // 1. Update KPIs
        kpiService.processInvestigationEvent(event);

        // 2. Write audit log
        reportingService.saveAuditLog(
                event.getUserId(), "Investigation", event.getInvestigationId(), "STATUS_CHANGE",
                "Investigation " + event.getInvestigationId() + " (Claim " + event.getClaimId()
                        + ") changed from " + event.getPreviousStatus() + " to " + event.getNewStatus());

        // 3. Send in-app notification
        if (event.getUserId() != null) {
            String message = "Investigation #" + event.getInvestigationId()
                    + " for Claim #" + event.getClaimId()
                    + " changed from " + event.getPreviousStatus()
                    + " to " + event.getNewStatus() + ".";
            notificationService.createNotification(
                    event.getUserId(), event.getClaimId(), message, "INVESTIGATION");
        }

        return ResponseEntity.ok("Event processed");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String buildClaimNotification(ClaimEventDTO event) {
        return switch (event.getNewStatus() == null ? "" : event.getNewStatus().toUpperCase()) {
            case "IN_REVIEW"  -> "Your Claim #" + event.getClaimId() + " is now under review.";
            case "SETTLED"    -> "Your Claim #" + event.getClaimId() + " has been settled.";
            case "REJECTED"   -> "Your Claim #" + event.getClaimId() + " has been rejected.";
            case "CLOSED"     -> "Your Claim #" + event.getClaimId() + " has been closed.";
            default           -> "Your Claim #" + event.getClaimId()
                                  + " status changed to " + event.getNewStatus() + ".";
        };
    }

    private String resolveClaimCategory(String status) {
        if (status == null) return "INTAKE";
        return switch (status.toUpperCase()) {
            case "IN_REVIEW", "CLOSED" -> "INVESTIGATION";
            case "SETTLED"             -> "PAYMENT";
            default                    -> "INTAKE";
        };
    }
}
