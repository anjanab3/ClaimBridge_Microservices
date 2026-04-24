package com.cts.claimbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimFullResponseDTO { //A
    private ClaimDTO claim;
    private InvestigationDTO investigation;
    private PolicyDTO policy;
    private PolicyHolderDTO policyHolder;
    private List<EvidenceDTO> evidences;
}