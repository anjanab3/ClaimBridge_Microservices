package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.repository.PolicyHolderRepository;
import com.cts.claimbridge.repository.PolicyRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

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
      for(Policy policy:policies){
          if(policyRepo.existsByPolicyNumber(policy.getPolicyNumber())){
              throw new RuntimeException("Policy Number already exists");
          }
      }
        return policyRepo.saveAll(policies);
    }

    public List<Long> findPolicyIdsByHolderId(Long holderId) {
    return policyRepo.findByHolderId(holderId)
            .stream()
            .map(Policy::getPolicyId)
            .toList();
}
}
