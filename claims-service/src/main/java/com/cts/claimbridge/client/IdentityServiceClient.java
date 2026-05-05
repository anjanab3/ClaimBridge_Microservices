package com.cts.claimbridge.client;

import com.cts.claimbridge.dto.TriageRuleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "identity-service")
public interface IdentityServiceClient {

    /** All active rules — used by the suggest logic */
    @GetMapping("/api/internal/triage/rules/active")
    List<TriageRuleDTO> getActiveRules();

    /** Single rule by ID — used when creating / updating a triage decision */
    @GetMapping("/api/internal/triage/rules/{ruleId}")
    TriageRuleDTO getRuleById(@PathVariable("ruleId") Long ruleId);

    /** Default rule — used by FraudAnalystService when routing to adjuster */
    @GetMapping("/api/internal/triage/rules/default")
    TriageRuleDTO getDefaultRule();

    /** Rules by assigned queue — used by FraudScoringService for auto-escalation */
    @GetMapping("/api/internal/triage/rules/queue/{queue}")
    List<TriageRuleDTO> getRulesByQueue(@PathVariable("queue") String queue);
}
