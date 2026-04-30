package com.cts.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "claims")
public interface ClaimsServiceClient {

    @PutMapping("/api/claims/{claimId}/status")
    void updateClaimStatus(
            @PathVariable("claimId") Long claimId,
            @RequestBody String status);
}