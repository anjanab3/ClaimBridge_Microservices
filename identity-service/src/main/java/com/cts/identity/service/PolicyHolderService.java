package com.cts.identity.service;

import com.cts.identity.dto.PolicyHolderRequestDTO;
import com.cts.identity.dto.PolicyHolderResponseDTO;
import com.cts.identity.entity.PolicyHolder;
import com.cts.identity.repository.PolicyHolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PolicyHolderService {

    @Autowired
    private PolicyHolderRepository policyHolderRepository;

    public Page<PolicyHolderResponseDTO> findAll(int page, int size) {
        return policyHolderRepository.findAll(PageRequest.of(page, size))
                .map(this::toDTO);
    }

    public PolicyHolderResponseDTO findById(Long holderId) {
        PolicyHolder holder = policyHolderRepository.findById(holderId)
                .orElseThrow(() -> new RuntimeException("PolicyHolder not found: " + holderId));
        return toDTO(holder);
    }

    public PolicyHolderResponseDTO create(PolicyHolderRequestDTO req) {
        PolicyHolder holder = new PolicyHolder();
        holder.setName(req.getName());
        holder.setContactInfo(req.getContactInfo());
        holder.setBusinessType(req.getBusinessType());
        holder.setTaxID(req.getTaxID());
        return toDTO(policyHolderRepository.save(holder));
    }

    public PolicyHolderResponseDTO update(Long holderId, PolicyHolderRequestDTO req) {
        PolicyHolder holder = policyHolderRepository.findById(holderId)
                .orElseThrow(() -> new RuntimeException("PolicyHolder not found: " + holderId));
        if (req.getName()        != null) holder.setName(req.getName());
        if (req.getContactInfo() != null) holder.setContactInfo(req.getContactInfo());
        if (req.getBusinessType()!= null) holder.setBusinessType(req.getBusinessType());
        if (req.getTaxID()       != null) holder.setTaxID(req.getTaxID());
        return toDTO(policyHolderRepository.save(holder));
    }

    private PolicyHolderResponseDTO toDTO(PolicyHolder h) {
        return new PolicyHolderResponseDTO(h.getHolderId(), h.getName(),
                h.getContactInfo(), h.getBusinessType(), h.getTaxID());
    }
}
