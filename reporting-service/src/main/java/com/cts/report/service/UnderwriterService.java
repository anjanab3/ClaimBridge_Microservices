package com.cts.report.service;

import com.cts.report.client.ClaimsServiceClient;
import com.cts.report.client.PolicyServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UnderwriterService {

    @Autowired private ClaimsServiceClient claimsServiceClient;
    @Autowired private PolicyServiceClient  policyServiceClient;

    // ── Claims ───────────────────────────────────────────────────────────────

    public Object getAllClaims(int page, int size) {
        return claimsServiceClient.getAllClaims(page, size).getBody();
    }

    public Object getClaimsByStatus(String status, int page, int size) {
        return claimsServiceClient.getClaimsByStatus(status, page, size).getBody();
    }

    public Object getClaimsByLossType(String lossType, int page, int size) {
        return claimsServiceClient.getClaimsByLossType(lossType, page, size).getBody();
    }

    public Object getClaimTrend() {
        return claimsServiceClient.getClaimTrend().getBody();
    }

    // ── Fraud Alerts ─────────────────────────────────────────────────────────

    public Object getAllFraudAlerts(int page, int size) {
        return claimsServiceClient.getAllFraudAlerts(page, size).getBody();
    }

    public Object getFraudAlertsByStatus(String status, int page, int size) {
        return claimsServiceClient.getFraudAlertsByStatus(status, page, size).getBody();
    }

    // ── Policies ─────────────────────────────────────────────────────────────

    public Object getAllPolicies(int page, int size) {
        return policyServiceClient.getAllPolicies(page, size).getBody();
    }

    public Object getPolicyById(Long policyId) {
        return policyServiceClient.getPolicyById(policyId).getBody();
    }

    public Object updatePolicyCoverage(Long policyId, String coverageJSON) {
        return policyServiceClient.updatePolicyCoverage(policyId, coverageJSON).getBody();
    }

    public Object updatePolicyStatus(Long policyId, String status) {
        return policyServiceClient.updatePolicyStatus(policyId, status).getBody();
    }
}
