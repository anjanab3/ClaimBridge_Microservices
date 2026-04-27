package com.cts.claimbridge.controller;


import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.FraudAlert;
import com.cts.claimbridge.service.FraudAnalystService;
import com.cts.claimbridge.service.TriageDecisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/triage/decisions")
@PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_INTAKE_AGENT')")
public class TriageDecisionController {
    @Autowired
    private TriageDecisionService decisionService;

    @Autowired
    private FraudAnalystService fraudService;

    @GetMapping("/queue")
    public ResponseEntity<Page<QueueResponseDTO>> getQueue(
            @RequestParam String type,
            @PageableDefault(size = 10) Pageable pageable) {

        return switch (type.toUpperCase()) {
            case "FRAUD" -> {
                Page<FraudAlert> alerts = fraudService.getAllFraudAlerts(pageable);
                List<QueueResponseDTO> result = new ArrayList<>();
                for (FraudAlert alert : alerts) {
                    result.add(new QueueResponseDTO("FRAUD", alert));
                }
                yield ResponseEntity.ok(new PageImpl<>(result, pageable, alerts.getTotalElements()));
            }
            case "ADJUSTER" -> {
                Page<TriageDecisionResponseDTO> decisions = decisionService.getDecisionsByAssignee("ADJUSTER", pageable);
                List<QueueResponseDTO> result = new ArrayList<>();
                for (TriageDecisionResponseDTO decision : decisions) {
                    result.add(new QueueResponseDTO("ADJUSTER", decision));
                }
                yield ResponseEntity.ok(new PageImpl<>(result, pageable, decisions.getTotalElements()));
            }
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid queue type '" + type + "'. Accepted values: FRAUD, ADJUSTER"
            );
        };
    }

    @PostMapping
    public ResponseEntity<?> createDecision(@RequestBody TriageDecisionRequestDTO request) {
        try {
            return ResponseEntity.ok().body(new MessageDTO(decisionService.createDecision(request),"Triage decision created successfully !!!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    @PutMapping("/{decisionId}")
    public ResponseEntity<?> updateDecision(
            @PathVariable Long decisionId,
            @RequestBody TriageDecisionRequestDTO request) {
        try {
            return ResponseEntity.ok().body(new MessageDTO(decisionService.updateDecision(decisionId, request),"Triage decision updated successfully !!!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO("No decision Id found!!!"));
        }
    }
//    @GetMapping("/claim/{claimId}")
//    public ResponseEntity<List<TriageDecisionResponseDTO>> getByClaimId(@PathVariable Long claimId) {
//        return ResponseEntity.ok(decisionService.getDecisionsByClaimId(claimId));
//    }

//    @GetMapping("/queue/fraud")
//    public ResponseEntity<Page<FraudAlert>> getFraudQueue(
//            @PageableDefault(size = 10) Pageable pageable) {
//        return ResponseEntity.ok(fraudService.getAllFraudAlerts(pageable));
//    }
//
//    @GetMapping("/queue/adjuster")
//    public ResponseEntity<Page<TriageDecisionResponseDTO>> getAdjusterQueue(
//            @PageableDefault(size = 10) Pageable pageable) {
//        return ResponseEntity.ok(decisionService.getDecisionsByAssignee("ADJUSTER", pageable));
//    }
//    @GetMapping("/{claimId}/triage_decision") //A
//    public TriageDecisionResponseDTO getTriageDecision(@PathVariable Long claimId) {
//        return decisionService.getLatestDecision(claimId);
//    }

}
