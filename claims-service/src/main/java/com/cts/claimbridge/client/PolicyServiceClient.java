package com.cts.claimbridge.client;

import com.cts.claimbridge.dto.PolicyDTO;
import com.cts.claimbridge.dto.PolicyHolderDTO;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "policy")
public interface PolicyServiceClient {

    @GetMapping("/api/policies/{policyId}")
    PolicyDTO getPolicyById(@PathVariable("policyId") Long policyId);

    @GetMapping("/api/policyholders/{holderId}")
    PolicyHolderDTO getPolicyHolderById(@PathVariable("holderId") Long holderId);

     // New — needed for frequent claimant rule
    @GetMapping("/api/policies/by-holder/{holderId}/ids")
    List<Long> getPolicyIdsByHolderId(@PathVariable("holderId") Long holderId);
}