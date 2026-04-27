package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.entity.PolicyHolder;
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
public class PolicyHolderService {
    private final PolicyHolderRepository holderRepo;
    private final PolicyRepository policyRepo;

    //find holder by id
    public Optional<PolicyHolder> findById(long id) {
        return holderRepo.findById(id);
    }

    // Get all policyholders
    public Page<PolicyHolder> findAll(int page , int size) {
        Pageable pageable = PageRequest.of(page,size);
        return holderRepo.findAll(pageable);
    }

    // Create policyholder
    public List<PolicyHolder> save(List<PolicyHolder> holder) {
        return holderRepo.saveAll(holder);
    }

    // Get all policies for a given holder
    public List<Policy> findPoliciesForHolder(Long holderId) {
        return policyRepo.findByHolderId(holderId);
    }

    //update the policyholder
    public PolicyHolder update(Long holderId,PolicyHolder holder){
        PolicyHolder existing=holderRepo.findById(holderId).orElseThrow(()->new RuntimeException("PolicyHolder not found"));
        existing.setName(holder.getName());
        existing.setContactInfo(holder.getContactInfo());;
        existing.setBusinessType(holder.getBusinessType());
        existing.setTaxID(holder.getTaxID());
        return holderRepo.save(existing);
    }
}
