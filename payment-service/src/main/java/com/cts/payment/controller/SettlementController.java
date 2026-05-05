package com.cts.payment.controller;

import com.cts.payment.dto.ResponseDTO;
import com.cts.payment.dto.SettlementSyncDTO;
import com.cts.payment.entity.Settlement;
import com.cts.payment.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    // Receive settlement pushed from claims-service — internal call no auth needed
    @PostMapping("/receive")
    public ResponseEntity<?> receiveSettlement(@RequestBody SettlementSyncDTO dto) {
        try {
            Settlement saved = settlementService.receiveSettlement(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO("Failed to receive settlement: " + e.getMessage()));
        }
    }

    // Get all settlements — payout officer overview
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @GetMapping
    public ResponseEntity<?> getAllSettlements() {
        List<Settlement> settlements = settlementService.getAllSettlements();
        return ResponseEntity.ok(settlements);
    }

    // Get settlement by claimId
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @GetMapping("/claim/{claimId}")
    public ResponseEntity<?> getSettlementByClaim(
            @PathVariable("claimId") Long claimId) {
        Optional<Settlement> settlement = settlementService.getSettlementsByClaim(claimId);
        if (settlement.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO("No Settlement Found for claimId: " + claimId));
        }
        return ResponseEntity.ok(settlement.get());
    }

    // Approve or reject settlement
    //@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
    @PostMapping("/{settlementId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable("settlementId") Long settlementId,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String newStatus = statusUpdate.get("status");
            settlementService.updateStatus(settlementId, newStatus);
            return ResponseEntity.ok(new ResponseDTO("Settlement status updated to " + newStatus));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDTO(e.getMessage()));
        }
    }
}