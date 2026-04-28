package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.FraudAnalystRequestDTO;
import com.cts.claimbridge.dto.FraudAnalystResponseDTO;
import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.FraudAlert;
import com.cts.claimbridge.service.FraudAnalystService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAuthority('FRAUD_ANALYST')")
@RequestMapping("/api/fraud")
public class FraudAnalystController {

    @Autowired
    private FraudAnalystService fraudService;

    @GetMapping("/alerts")
    public ResponseEntity<?> getAllAlerts(
            @PageableDefault(size = 10) Pageable pageable) {
        Page<FraudAlert> allfraudalerts = fraudService.getAllFraudAlerts(pageable);
        if(allfraudalerts.isEmpty())
        {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Fraud Alerts Found"));
        }
        return ResponseEntity.ok().body(allfraudalerts);
    }

    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<?> getAlertById(@PathVariable Long alertId) {
        return ResponseEntity.ok(fraudService.getAlertById(alertId));
    }

    @GetMapping("/score/{claimId}")
    public ResponseEntity<Object> getFraudScore(@PathVariable Long claimId) {
        return ResponseEntity.ok(fraudService.getFraudScore(claimId));
    }

    @PutMapping("/alerts/{alertId}/action")
    public ResponseEntity<FraudAnalystResponseDTO> takeAction(
            @PathVariable Long alertId,
            @RequestBody FraudAnalystRequestDTO request) {

        switch (request.getDecision().toUpperCase()) {
            case "CLEAR", "ESCALATE", "REJECT" -> {
                return ResponseEntity.ok(fraudService.takeDecision(alertId, request));
            }
            case "REASSIGN" -> {
                return ResponseEntity.ok(fraudService.reassignToAdjuster(alertId, request));
            }
            default -> throw new EntityNotFoundException(
                    "Invalid decision. Allowed: CLEAR, ESCALATE, REJECT, REASSIGN");
        }
    }

    @PostMapping("/investigate/{claimId}")
    public ResponseEntity<?> updateFraudAlert(
            @PathVariable Long claimId,
            @RequestBody FraudAnalystRequestDTO request) {
        try {
            return ResponseEntity.ok().body(new MessageDTO(fraudService.updateAlert(claimId, request),"Fraud data added !!!"));
        }
        catch(Exception e)
        {
            return ResponseEntity.badRequest().body(new ResponseDTO("No claim Id found"));
        }
    }

    @PostMapping("/notes")
    public String addNotes(@RequestBody FraudAnalystRequestDTO request) {
        return fraudService.addNotes(request.getNote());
    }

}
