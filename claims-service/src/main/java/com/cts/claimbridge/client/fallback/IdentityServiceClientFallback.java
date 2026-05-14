package com.cts.claimbridge.client.fallback;

import com.cts.claimbridge.client.IdentityServiceClient;
import com.cts.claimbridge.dto.TriageRuleDTO;
import com.cts.claimbridge.dto.UserDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class IdentityServiceClientFallback implements IdentityServiceClient {

    @Override
    public List<TriageRuleDTO> getActiveRules() {
        log.warn("[CircuitBreaker] identity-service unavailable — returning empty triage rules list");
        return Collections.emptyList();
    }

    @Override
    public TriageRuleDTO getRuleById(Long ruleId) {
        log.warn("[CircuitBreaker] identity-service unavailable — cannot fetch rule {}", ruleId);
        return null;
    }

    @Override
    public TriageRuleDTO getDefaultRule() {
        log.warn("[CircuitBreaker] identity-service unavailable — cannot fetch default rule");
        return null;
    }

    @Override
    public List<TriageRuleDTO> getRulesByQueue(String queue) {
        log.warn("[CircuitBreaker] identity-service unavailable — returning empty rules for queue {}", queue);
        return Collections.emptyList();
    }

    @Override
    public UserDTO getUserById(String userId) {
        log.warn("[CircuitBreaker] identity-service unavailable — cannot fetch user {}", userId);
        return null;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        log.warn("[CircuitBreaker] identity-service unavailable — cannot fetch user by username {}", username);
        return null;
    }

    @Override
    public List<UserDTO> getUsersByRole(String role) {
        log.warn("[CircuitBreaker] identity-service unavailable — returning empty users for role {}", role);
        return Collections.emptyList();
    }
}
