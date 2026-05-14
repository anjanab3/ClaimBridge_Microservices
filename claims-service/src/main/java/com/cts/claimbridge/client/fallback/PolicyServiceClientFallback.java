package com.cts.claimbridge.client.fallback;

import com.cts.claimbridge.client.PolicyServiceClient;
import com.cts.claimbridge.dto.PolicyDTO;
import com.cts.claimbridge.dto.PolicyHolderDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class PolicyServiceClientFallback implements PolicyServiceClient {

    @Override
    public PolicyDTO getPolicyById(Long policyId) {
        log.warn("[CircuitBreaker] policy-service unavailable — cannot fetch policy {}", policyId);
        return null;
    }

    @Override
    public PolicyHolderDTO getPolicyHolderById(Long holderId) {
        log.warn("[CircuitBreaker] policy-service unavailable — cannot fetch policyholder {}", holderId);
        return null;
    }

    @Override
    public List<Long> getPolicyIdsByHolderId(Long holderId) {
        log.warn("[CircuitBreaker] policy-service unavailable — returning empty policy IDs for holder {}", holderId);
        return Collections.emptyList();
    }
}
