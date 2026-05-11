package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.repository.PolicyHolderRepository;
import com.cts.claimbridge.repository.PolicyRepository;
import com.cts.claimbridge.util.PolicyStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepo;
    
    public Optional<Policy> findByNumber(String number) {
        return policyRepo.findByPolicyNumber(number);
    }

    public Optional<Policy> findById(Long id) {
        return policyRepo.findById(id);
    }

    public Page<Policy> findAll(int page , int size) {
        Pageable pageable = PageRequest.of(page , size);
        return policyRepo.findAll(pageable);
    }

    public List<Policy> savePolicies(List<Policy> policies) {
        log.info("Saving {} policies", policies.size());
        for (Policy policy : policies) {
            if (policyRepo.existsByPolicyNumber(policy.getPolicyNumber())) {
                log.warn("Duplicate policy number: {}", policy.getPolicyNumber());
                throw new RuntimeException("Policy Number already exists");
            }
        }
        List<Policy> saved = policyRepo.saveAll(policies);
        log.info("Saved {} policies successfully", saved.size());
        return saved;
    }

    public List<Long> findPolicyIdsByHolderId(Long holderId) {
        return policyRepo.findByHolderId(holderId)
                .stream()
                .map(Policy::getPolicyId)
                .toList();
    }

    /** Update coverageJSON on an existing policy. */
    public Policy updateCoverage(Long policyId, String coverageJSON) {
        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));
        policy.setCoverageJSON(coverageJSON);
        return policyRepo.save(policy);
    }

    /** Update the status of an existing policy. */
    public Policy updateStatus(Long policyId, String status) {
        Policy policy = policyRepo.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));
        policy.setStatus(PolicyStatus.valueOf(status.toUpperCase()));
        return policyRepo.save(policy);
    }
}
