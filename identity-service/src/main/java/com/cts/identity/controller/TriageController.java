package com.cts.identity.controller;

import com.cts.identity.dto.MessageDTO;
import com.cts.identity.dto.ResponseDTO;
import com.cts.identity.dto.TriageDecisionRequestDTO;
import com.cts.identity.dto.TriageRuleRequestDTO;
import com.cts.identity.service.TriageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/identity/triage")
public class TriageController {

    @Autowired private TriageService triageService;

    // ── Rules ────────────────────────────────────────────────────────────────

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/rules")
    public ResponseEntity<?> getRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(triageService.getAllRules(page, size));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/rules")
    public ResponseEntity<?> createRule(@RequestBody TriageRuleRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageDTO(triageService.createRule(req), "Rule created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<?> updateRule(@PathVariable Long ruleId, @RequestBody TriageRuleRequestDTO req) {
        try {
            return ResponseEntity.ok(new MessageDTO(triageService.updateRule(ruleId, req), "Rule updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<?> deleteRule(@PathVariable Long ruleId) {
        try {
            triageService.deleteRule(ruleId);
            return ResponseEntity.ok(new ResponseDTO("Rule deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/rules/{ruleId}/toggle")
    public ResponseEntity<?> toggleRule(@PathVariable Long ruleId) {
        try {
            return ResponseEntity.ok(new MessageDTO(triageService.toggleRule(ruleId), "Rule status toggled"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    // ── Decisions ────────────────────────────────────────────────────────────

    @PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_INTAKE_AGENT')")
    @PostMapping("/decisions")
    public ResponseEntity<?> createDecision(@RequestBody TriageDecisionRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(triageService.createDecision(req));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_INTAKE_AGENT','CLAIMS_ADJUSTER')")
    @GetMapping("/decisions/{claimId}")
    public ResponseEntity<?> getDecisionsByClaimId(@PathVariable Long claimId) {
        try {
            return ResponseEntity.ok(triageService.getDecisionsByClaimId(claimId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_INTAKE_AGENT','CLAIMS_ADJUSTER')")
    @GetMapping("/decisions/assigned/{assignedTo}")
    public ResponseEntity<?> getDecisionsByAssignee(@PathVariable String assignedTo) {
        return ResponseEntity.ok(triageService.getDecisionsByAssignee(assignedTo));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_INTAKE_AGENT')")
    @PutMapping("/decisions/{decisionId}")
    public ResponseEntity<?> updateDecision(@PathVariable Long decisionId,
                                            @RequestBody TriageDecisionRequestDTO req) {
        try {
            return ResponseEntity.ok(triageService.updateDecision(decisionId, req));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }
}
