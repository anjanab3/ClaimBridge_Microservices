package com.cts.report.controller;

import com.cts.report.dto.SLARequestDTO;
import com.cts.report.dto.SLAResponseDTO;
import com.cts.report.dto.SLABreachDTO;
import com.cts.report.service.SLAService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla")
public class SLAController {

    @Autowired
    private SLAService slaService;

    /*
        GET /api/sla/query
        type = ALL_SLAS                             → all SLA rules
        type = BY_ENTITY  &entity={monitoredEntity} → CLAIM, INVESTIGATION, FRAUD_ALERT, SETTLEMENT
    */
    @PreAuthorize("hasAuthority('AUDITOR') or hasAuthority('COMPLIANCE') or hasAuthority('UNDERWRITER')")
    @GetMapping("/query")
    public ResponseEntity<Object> querySLAs(
            @RequestParam String type,
            @RequestParam(required = false) String entity) {

        switch (type.toUpperCase()) {

            case "ALL_SLAS" -> {
                return ResponseEntity.ok(slaService.getAllSLAs());
            }
            case "BY_ENTITY" -> {
                if (entity == null || entity.isBlank())
                    throw new IllegalArgumentException(
                            "entity is required for type BY_ENTITY. Allowed: CLAIM, INVESTIGATION, FRAUD_ALERT, SETTLEMENT");
                return ResponseEntity.ok(slaService.getSLAsByEntity(entity));
            }
            default -> throw new EntityNotFoundException(
                    "Invalid type. Allowed values: ALL_SLAS, BY_ENTITY");
        }
    }

    // POST /api/sla — create a new SLA rule (ADMIN only)
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<SLAResponseDTO> createSLA(@RequestBody SLARequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(slaService.createSLA(request));
    }

    // PUT /api/sla/{id} — update an existing SLA rule including toggling active (ADMIN only)
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<SLAResponseDTO> updateSLA(
            @PathVariable Long id,
            @RequestBody SLARequestDTO request) {
        return ResponseEntity.ok(slaService.updateSLA(id, request));
    }

    // GET /api/sla/breaches — live SLA breach report from claims-service data
    @PreAuthorize("hasAuthority('AUDITOR') or hasAuthority('COMPLIANCE') or hasAuthority('UNDERWRITER')")
    @GetMapping("/breaches")
    public ResponseEntity<List<SLABreachDTO>> getBreaches() {
        return ResponseEntity.ok(slaService.computeBreaches());
    }
}
