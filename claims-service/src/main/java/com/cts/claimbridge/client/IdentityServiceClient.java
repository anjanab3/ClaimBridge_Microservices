package com.cts.claimbridge.client;

import com.cts.claimbridge.dto.TriageRuleDTO;
import com.cts.claimbridge.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "identity-service", url = "${identity.service.url:http://localhost:9093}")
public interface IdentityServiceClient {

    // ── Triage rules ──────────────────────────────────────────────────────────

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

    // ── User lookups — replaces local user table entirely ─────────────────────

    /** Get a user by their userId (e.g. "CA-0001") */
    @GetMapping("/api/internal/users/{userId}")
    UserDTO getUserById(@PathVariable("userId") String userId);

    /** Get a user by their username */
    @GetMapping("/api/internal/users/by-username/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    /** Get all active users for a given role (e.g. CLAIMS_ADJUSTER) */
    @GetMapping("/api/internal/users/by-role")
    List<UserDTO> getUsersByRole(@RequestParam("role") String role);
}
