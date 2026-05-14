package com.cts.payment.client.fallback;

import com.cts.payment.client.ClaimsServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClaimsServiceClientFallback implements ClaimsServiceClient {

    @Override
    public void updateClaimStatus(Long claimId, String status) {
        log.error("[CircuitBreaker] claims-service unavailable — could not update status to '{}' for claim {}. Manual reconciliation required.",
                status, claimId);
    }
}
