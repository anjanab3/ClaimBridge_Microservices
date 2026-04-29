package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.service.InvestigationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
//@PreAuthorize("hasAnyAuthority('FRAUD_ANALYST','CLAIMS_ADJUSTER')")
public class InvestigationController {
    @Autowired
    private InvestigationService service;

    @GetMapping("/claims/{claimId}/investigation")
    public ResponseEntity<?> getInvestigationByClaimId(@PathVariable Long claimId) {
        try {
            InvestigationFullResponseDTO result = service.getInvestigationByClaimId(claimId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/investigations/{investigationId}")
    public ResponseEntity<?> updateInvestigationAndCreateSettlement(
            @PathVariable("investigationId") Long investigationId,
            @RequestBody InvestigateUpdateStatusDTO dto) {
        try {
            return ResponseEntity.ok().body(new MessageDTO(service.updateInvestigationAndCreateSettlement(investigationId, dto),"Status/Settlement updated successfully!!!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO("No investigation Id found"));
        }
    }
}