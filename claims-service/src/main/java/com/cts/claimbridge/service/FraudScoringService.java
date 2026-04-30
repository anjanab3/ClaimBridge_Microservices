package com.cts.claimbridge.service;

import com.cts.claimbridge.client.PolicyServiceClient;
import com.cts.claimbridge.dto.FraudScoringResultDTO;
import com.cts.claimbridge.dto.PolicyDTO;
import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.*;
import com.cts.claimbridge.util.TriageStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class FraudScoringService {

    private static final int RAPID_CLAIM_SCORE          = 50;
    private static final int RECENT_POLICY_CHANGE_SCORE = 30;
    private static final int FREQUENT_CLAIMANT_SCORE    = 30;
    private static final int DELAYED_REPORTING_SCORE    = 30;
    private static final int PERFECT_CLAIM_SCORE        = 30;
    private static final int MISSING_INFORMATION_SCORE  = 30;

    private static final int    RAPID_CLAIM_DAYS        = 7;
    private static final int    RECENT_CHANGE_DAYS      = 30;
    private static final int    DELAYED_REPORTING_DAYS  = 30;
    private static final int    FREQUENT_CLAIM_DAYS     = 70;
    private static final double PERFECT_CLAIM_TOLERANCE = 0.05;

    public static final int LOW_RISK_MAX    = 40;
    public static final int MEDIUM_RISK_MAX = 80;

    @Autowired private FraudScoreRepository     fraudScoreRepository;
    @Autowired private FraudAlertRepository     fraudAlertRepository;
    @Autowired private ClaimRepository          claimRepository;
    @Autowired private TriageRuleRepository     triageRuleRepository;
    @Autowired private TriageDecisionRepository triageDecisionRepository;
    @Autowired private PolicyServiceClient      policyServiceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public FraudScoringResultDTO scoreAndPersist(Claim claim) {

        // Fetch policy once from policy-service — all rules use this
        PolicyDTO policy = fetchPolicy(claim.getPolicyId());

        List<Map<String, Object>> triggered = new ArrayList<>();
        int total = 0;

        // Rule 1 : Rapid Claim
        if (policy != null
                && policy.getEffectiveDate() != null
                && claim.getCreatedDate() != null) {

            long days = ChronoUnit.DAYS.between(
                    policy.getEffectiveDate(),
                    claim.getCreatedDate().toLocalDate());

            if (days >= 0 && days <= RAPID_CLAIM_DAYS) {
                total += RAPID_CLAIM_SCORE;
                triggered.add(rule("RAPID_CLAIM", RAPID_CLAIM_SCORE,
                        "Claim submitted " + days + " day(s) after policy inception"));
            }
        }

        // Rule 2 : Recent Policy Changes (from coverageJSON.lastModified)
        LocalDate lastModified = parseDate(policy, "lastModified");
        if (lastModified != null && claim.getCreatedDate() != null) {
            long days = ChronoUnit.DAYS.between(lastModified, claim.getCreatedDate().toLocalDate());
            if (days >= 0 && days <= RECENT_CHANGE_DAYS) {
                total += RECENT_POLICY_CHANGE_SCORE;
                triggered.add(rule("RECENT_POLICY_CHANGES", RECENT_POLICY_CHANGE_SCORE,
                        "Coverage terms modified " + days + " day(s) before claim"));
            }
        }

        // Rule 3 : Delayed Reporting
        if (claim.getIncidentDate() != null && claim.getCreatedDate() != null) {
            long days = ChronoUnit.DAYS.between(
                    claim.getIncidentDate(),
                    claim.getCreatedDate().toLocalDate());

            if (days > DELAYED_REPORTING_DAYS) {
                total += DELAYED_REPORTING_SCORE;
                triggered.add(rule("DELAYED_REPORTING", DELAYED_REPORTING_SCORE,
                        "Reported " + days + " day(s) after incident (threshold: "
                                + DELAYED_REPORTING_DAYS + " days)"));
            }
        }

        // Rule 4 : Perfect Claim (from coverageJSON.deductible)
        Double deductible = parseDeductible(policy);
        if (deductible != null && claim.getEstimatedAmount() != null) {
            double claimed = claim.getEstimatedAmount();
            if (claimed < deductible && claimed >= deductible * (1 - PERFECT_CLAIM_TOLERANCE)) {
                total += PERFECT_CLAIM_SCORE;
                triggered.add(rule("PERFECT_CLAIM", PERFECT_CLAIM_SCORE,
                        "Claimed amount (" + claimed + ") is within 5% below deductible ("
                                + deductible + ")"));
            }
        }

        // Rule 5 : Missing Information
        if (claim.getEvidenceList() == null || claim.getEvidenceList().isEmpty()) {
            total += MISSING_INFORMATION_SCORE;
            triggered.add(rule("MISSING_INFORMATION", MISSING_INFORMATION_SCORE,
                    "No supporting evidence documents uploaded with the claim"));
        }

        // Rule 6 : Frequent Claimant — use holderId from policy
        if (policy != null
                && policy.getHolderId() != null
                && claim.getCreatedDate() != null) {

            LocalDateTime cutoff = claim.getCreatedDate().minusDays(FREQUENT_CLAIM_DAYS);
            long count = claimRepository.countByPolicyIdInAndCreatedDateAfter(
                    getClaimPolicyIdsByHolder(policy.getHolderId()), cutoff);

            if (count > 1) {
                total += FREQUENT_CLAIMANT_SCORE;
                triggered.add(rule("FREQUENT_CLAIMANT", FREQUENT_CLAIMANT_SCORE,
                        count + " claims filed by this policyholder in the last "
                                + FREQUENT_CLAIM_DAYS + " days"));
            }
        }

        // Build & persist FraudScore
        String riskBand    = riskBand(total);
        String factorsJson = buildFactorsJson(total, riskBand, triggered);

        FraudScore score = fraudScoreRepository
                .findByClaim_ClaimId(claim.getClaimId())
                .orElse(new FraudScore());
        score.setClaim(claim);
        score.setScoreValue((double) total);
        score.setFactorsJSON(factorsJson);
        score.setCalculatedAt(LocalDateTime.now());
        FraudScore saved = fraudScoreRepository.save(score);

        boolean escalated = "RED".equals(riskBand) && autoEscalate(claim, saved);

        return FraudScoringResultDTO.builder()
                .scoreId(saved.getScoreId())
                .claimId(claim.getClaimId())
                .totalScore(total)
                .riskBand(riskBand)
                .triggeredRules(triggered)
                .calculatedAt(saved.getCalculatedAt())
                .autoEscalated(escalated)
                .build();
    }

    private boolean autoEscalate(Claim claim, FraudScore score) {
        if (!fraudAlertRepository.findByClaim_ClaimId(claim.getClaimId()).isEmpty())
            return false;

        FraudAlert alert = new FraudAlert();
        alert.setClaim(claim);
        alert.setReason("Auto-escalated: fraud score " + score.getScoreValue().intValue() + " (RED band ≥ 90)");
        alert.setStatus("OPEN");
        alert.setEscalatedAt(LocalDateTime.now());
        FraudAlert savedAlert = fraudAlertRepository.save(alert);
        score.setFraudAlert(savedAlert);
        score.setClaim(claim);

        List<TriageRule> fraudRules = triageRuleRepository.findByAssignedQueue("FRAUD");
        if (!fraudRules.isEmpty()) {
            TriageRule rule = fraudRules.get(0);
            boolean alreadyQueued = triageDecisionRepository
                    .findTopByClaimIdOrderByAssignedAtDesc(claim.getClaimId())
                    .map(d -> "FRAUD".equalsIgnoreCase(d.getAssignedQueue()))
                    .orElse(false);

            if (!alreadyQueued) {
                TriageDecision decision = new TriageDecision();
                decision.setClaimId(claim.getClaimId());
                decision.setRuleId(rule.getRuleID());
                decision.setPriority(rule.getPriority());
                decision.setAssignedQueue("FRAUD");
                decision.setStatus(TriageStatus.OPEN);
                decision.setAssignedAt(LocalDateTime.now());
                triageDecisionRepository.save(decision);
            }
        }
        return true;
    }

    // ── Feign helpers ─────────────────────────────────────────────────────────

    private PolicyDTO fetchPolicy(Long policyId) {
        try {
            if (policyId == null) return null;
            return policyServiceClient.getPolicyById(policyId);
        } catch (Exception e) {
            return null;
        }
    }

    // Get all policyIds belonging to the same holder — used for frequent claimant rule
    private List<Long> getClaimPolicyIdsByHolder(Long holderId) {
        try {
            return policyServiceClient.getPolicyIdsByHolderId(holderId);
        } catch (Exception e) {
            return List.of();
        }
    }

    // ── coverageJSON parsers — now take PolicyDTO instead of Claim ────────────

    private Double parseDeductible(PolicyDTO policy) {
        Map<String, Object> coverage = parseCoverageJson(policy);
        if (coverage == null) return null;
        Object val = coverage.get("deductible");
        return val instanceof Number ? ((Number) val).doubleValue() : null;
    }

    private LocalDate parseDate(PolicyDTO policy, String fieldName) {
        Map<String, Object> coverage = parseCoverageJson(policy);
        if (coverage == null) return null;
        Object val = coverage.get(fieldName);
        if (val == null) return null;
        try {
            return LocalDate.parse(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> parseCoverageJson(PolicyDTO policy) {
        if (policy == null) return null;
        String json = policy.getCoverageJSON();
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private String riskBand(int score) {
        if (score <= LOW_RISK_MAX)    return "GREEN";
        if (score <= MEDIUM_RISK_MAX) return "YELLOW";
        return "RED";
    }

    private Map<String, Object> rule(String name, int points, String description) {
        Map<String, Object> entry = new LinkedHashMap<>();
        entry.put("rule", name);
        entry.put("points", points);
        entry.put("description", description);
        return entry;
    }

    private String buildFactorsJson(int total, String band, List<Map<String, Object>> triggered) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("totalScore", total);
        payload.put("riskBand", band);
        payload.put("triggeredRules", triggered);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            return "{}";
        }
    }
}