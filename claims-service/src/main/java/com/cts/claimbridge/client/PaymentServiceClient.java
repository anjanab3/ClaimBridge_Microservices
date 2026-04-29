package com.cts.claimbridge.client;

import com.cts.claimbridge.dto.SettlementSyncDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentServiceClient {

    @PostMapping("/api/settlement/receive")
    void sendSettlementToPayment(@RequestBody SettlementSyncDTO settlement);
}