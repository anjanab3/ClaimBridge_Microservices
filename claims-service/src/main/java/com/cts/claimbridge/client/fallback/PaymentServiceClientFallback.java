package com.cts.claimbridge.client.fallback;

import com.cts.claimbridge.client.PaymentServiceClient;
import com.cts.claimbridge.dto.SettlementSyncDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentServiceClientFallback implements PaymentServiceClient {

    @Override
    public void sendSettlementToPayment(SettlementSyncDTO settlement) {
        log.error("[CircuitBreaker] payment-service unavailable — settlement for claim {} could not be forwarded. Manual retry required.",
                settlement != null ? settlement.getClaimId() : "unknown");
    }
}
