package com.cts.identity.service;

import com.cts.identity.dto.PolicyRequestDTO;
import com.cts.identity.dto.PolicyResponseDTO;
import com.cts.identity.entity.Policy;
import com.cts.identity.entity.PolicyHolder;
import com.cts.identity.repository.PolicyHolderRepository;
import com.cts.identity.repository.PolicyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyHolderRepository policyHolderRepository;

    public Page<PolicyResponseDTO> findAll(int page, int size) {
        return policyRepository.findAll(PageRequest.of(page, size))
                .map(this::toDTO);
    }

    public PolicyResponseDTO findById(Long policyId) {
        return toDTO(policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId)));
    }

    public PolicyResponseDTO create(PolicyRequestDTO req) {
        if (policyRepository.existsByPolicyNumber(req.getPolicyNumber()))
            throw new RuntimeException("Policy number already exists: " + req.getPolicyNumber());

        PolicyHolder holder = policyHolderRepository.findById(req.getHolderId())
                .orElseThrow(() -> new RuntimeException("PolicyHolder not found: " + req.getHolderId()));

        Policy policy = new Policy();
        policy.setPolicyNumber(req.getPolicyNumber());
        policy.setInsuredName(req.getInsuredName());
        policy.setEffectiveDate(req.getEffectiveDate());
        policy.setExpiryDate(req.getExpiryDate());
        policy.setCoverageJSON(req.getCoverageJSON());
        policy.setStatus(req.getStatus());
        policy.setHolder(holder);
        return toDTO(policyRepository.save(policy));
    }

    public PolicyResponseDTO update(Long policyId, PolicyRequestDTO req) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found: " + policyId));
        if (req.getInsuredName()   != null) policy.setInsuredName(req.getInsuredName());
        if (req.getEffectiveDate() != null) policy.setEffectiveDate(req.getEffectiveDate());
        if (req.getExpiryDate()    != null) policy.setExpiryDate(req.getExpiryDate());
        if (req.getCoverageJSON()  != null) policy.setCoverageJSON(req.getCoverageJSON());
        if (req.getStatus()        != null) policy.setStatus(req.getStatus());
        if (req.getHolderId()      != null) {
            PolicyHolder holder = policyHolderRepository.findById(req.getHolderId())
                    .orElseThrow(() -> new RuntimeException("PolicyHolder not found: " + req.getHolderId()));
            policy.setHolder(holder);
        }
        return toDTO(policyRepository.save(policy));
    }

    private PolicyResponseDTO toDTO(Policy p) {
        return new PolicyResponseDTO(p.getPolicyId(), p.getPolicyNumber(), p.getInsuredName(),
                p.getEffectiveDate(), p.getExpiryDate(), p.getCoverageJSON(), p.getStatus(),
                p.getHolder() != null ? p.getHolder().getHolderId() : null);
    }
}
