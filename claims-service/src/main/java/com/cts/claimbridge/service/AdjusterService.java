package com.cts.claimbridge.service;

import com.cts.claimbridge.client.PolicyServiceClient;
import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.*;
import com.cts.claimbridge.util.InvestigationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdjusterService {

    @Autowired
    private TriageDecisionRepository triageRepo;
    @Autowired
    private ClaimRepository claimRepo;
    @Autowired
    private InvestigationRepository investigationRepo;
    @Autowired
    private EvidenceRepository evidenceRepo;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PolicyServiceClient policyServiceClient;

    public Page<ClaimFullResponseDTO> getAssignedClaims(String adjusterId, int page, int size) {
        List<TriageDecision> decisions = triageRepo.findByAssignedTo(adjusterId);

        Optional<User> adjuster = userRepository.findByRoleCode(adjusterId);
        if (adjuster.isEmpty()) {
            throw new RuntimeException("No Adjuster Found");
        }

        List<ClaimFullResponseDTO> resultList = decisions.stream()
                .map(decision -> {
                    Claim claim = claimRepo.findById(decision.getClaim().getClaimId())
                            .orElseThrow(() -> new RuntimeException("Claim not found"));

                    Investigation investigation = investigationRepo
                            .findByClaim_ClaimId(claim.getClaimId())
                            .orElseGet(() -> createInvestigation(claim));

                    investigation.setStatus(InvestigationStatus.OPEN);
                    investigationRepo.save(investigation);

                    Long policyId = claim.getPolicyId();
                    PolicyDTO policy = fetchPolicy(policyId);
                    PolicyHolderDTO holder = fetchPolicyHolder(policy != null ? policy.getHolderId() : null);
                    System.out.println("Fetched holder: " + holder); 
                    List<Evidence> evidences = evidenceRepo.findByClaim_ClaimId(claim.getClaimId());

                    return mapToFullDTO(claim, investigation, policy, holder, evidences);
                })
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + size, resultList.size());

        if (start > resultList.size()) {
            return new PageImpl<>(List.of(), pageable, resultList.size());
        }

        return new PageImpl<>(resultList.subList(start, end), pageable, resultList.size());
    }

    public ClaimFullResponseDTO getAssignedClaimsById(String adjusterId, Long claimId) {
        Optional<Claim> claimOpt = claimRepo.findById(claimId);
        if (claimOpt.isEmpty()) {
            return null;
        }

        Claim claim = claimOpt.get();

        Investigation investigation = investigationRepo.findByClaim_ClaimId(claimId)
                .orElseGet(() -> createInvestigation(claim));

        investigation.setStatus(InvestigationStatus.OPEN);
        investigationRepo.save(investigation);

        Long policyId = claim.getPolicyId();
        PolicyDTO policy = fetchPolicy(policyId);
        PolicyHolderDTO holder = fetchPolicyHolder(policy != null ? policy.getHolderId() : null);
        List<Evidence> evidences = evidenceRepo.findByClaim_ClaimId(claimId);

        return mapToFullDTO(claim, investigation, policy, holder, evidences);
    }

    private PolicyDTO fetchPolicy(Long policyId) {
        try {
            return policyServiceClient.getPolicyById(policyId);
        } catch (Exception e) {
            return null;
        }
    }

    private PolicyHolderDTO fetchPolicyHolder(Long holderId) {
        try {
            if (holderId == null) return null;
            return policyServiceClient.getPolicyHolderById(holderId);
        } catch (Exception e) {
            return null;
        }
    }

    private Investigation createInvestigation(Claim claim) {
        Investigation inv = new Investigation();
        inv.setClaim(claim);
        inv.setStatus(InvestigationStatus.OPEN);
        inv.setOpenedAt(LocalDateTime.now());
        return investigationRepo.save(inv);
    }

    private ClaimFullResponseDTO mapToFullDTO(Claim claim, Investigation investigation,
                                              PolicyDTO policy, PolicyHolderDTO holder,
                                              List<Evidence> evidenceList) {
        ClaimFullResponseDTO dto = new ClaimFullResponseDTO();

        ClaimDTO claimDTO = new ClaimDTO();
        claimDTO.setClaimId(claim.getClaimId());
        claimDTO.setPolicyId(claim.getPolicyId());
        claimDTO.setReportedBy(claim.getReportedBy());
        claimDTO.setIncidentDate(claim.getIncidentDate());
        claimDTO.setLossType(claim.getLossType());
        claimDTO.setEstimatedAmount(claim.getEstimatedAmount());
        claimDTO.setStatus(claim.getStatus().name());
        claimDTO.setCreatedAt(claim.getCreatedDate());
        dto.setClaim(claimDTO);

        InvestigationDTO invDTO = new InvestigationDTO();
        invDTO.setInvestigationId(investigation.getInvestigationId());
        invDTO.setAssignedAdjusterId(investigation.getAssignedAdjusterId());
        invDTO.setStatus(investigation.getStatus().name());
        invDTO.setOpenedAt(investigation.getOpenedAt());
        invDTO.setClosedAt(investigation.getClosedAt());
        dto.setInvestigation(invDTO);

        if (policy != null) {
            dto.setPolicy(policy);
        }

        if (holder != null) {
            dto.setPolicyHolder(holder);
        }

        List<EvidenceDTO> evidenceDTOList = evidenceList.stream().map(ev -> {
            EvidenceDTO ed = new EvidenceDTO();
            ed.setEvidenceId(ev.getEvidenceId());
            ed.setFileName(ev.getFileName());
            ed.setFileType(ev.getFileType());
            ed.setFilePath(ev.getFilePath());
            ed.setUploadedAt(ev.getUploadedAt());
            return ed;
        }).collect(Collectors.toList());
        dto.setEvidences(evidenceDTOList);

        return dto;
    }
}