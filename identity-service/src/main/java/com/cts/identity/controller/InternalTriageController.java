package com.cts.identity.controller;

import com.cts.identity.dto.TriageRuleResponseDTO;
import com.cts.identity.repository.TriageRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Internal service-to-service endpoints — no JWT required.
 * Only called by other microservices (e.g. claims-service via Feign).
 */
@RestController
@RequestMapping("/api/internal/triage/rules")
public class InternalTriageController {

    @Autowired
    private TriageRuleRepository ruleRepository;

    private TriageRuleResponseDTO toDTO(com.cts.identity.entity.TriageRule r) {
        return TriageRuleResponseDTO.builder()
                .ruleId(r.getRuleId())
                .name(r.getName())
                .conditionsJSON(r.getConditionsJSON())
                .priority(r.getPriority())
                .assignedQueue(r.getAssignedQueue())
                .active(r.getActive())
                .isDefault(r.getIsDefault())
                .build();
    }

    /** All active rules — used by claims-service for suggest logic */
    @GetMapping("/active")
    public List<TriageRuleResponseDTO> getActiveRules() {
        return ruleRepository.findByActive(true)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    /** Single rule by ID — used by claims-service when applying a rule */
    @GetMapping("/{ruleId}")
    public ResponseEntity<TriageRuleResponseDTO> getRuleById(@PathVariable Long ruleId) {
        return ruleRepository.findById(ruleId)
                .map(r -> ResponseEntity.ok(toDTO(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Default rule — used by claims-service FraudAnalystService */
    @GetMapping("/default")
    public ResponseEntity<TriageRuleResponseDTO> getDefaultRule() {
        return ruleRepository.findByIsDefaultTrue()
                .map(r -> ResponseEntity.ok(toDTO(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Rules by assigned queue — used by claims-service FraudScoringService */
    @GetMapping("/queue/{queue}")
    public List<TriageRuleResponseDTO> getRulesByQueue(@PathVariable String queue) {
        return ruleRepository.findByAssignedQueue(queue)
                .stream().map(this::toDTO).collect(Collectors.toList());
    }
}
