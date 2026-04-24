package com.cts.claimbridge.service;

import com.cts.claimbridge.util.ClaimStatus;
import com.cts.claimbridge.util.InvestigationStatus;
import com.cts.claimbridge.util.Status;
import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.Investigation;
import com.cts.claimbridge.entity.InvestigationNote;
import com.cts.claimbridge.entity.Settlement;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.InvestigationNoteRepository;
import com.cts.claimbridge.repository.InvestigationRepository;
import com.cts.claimbridge.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InvestigationService {
    @Autowired
    private InvestigationRepository investigationRepo;
    @Autowired
    private InvestigationNoteRepository noteRepo;
    @Autowired
    private ClaimRepository claimRepo;
    @Autowired
    private SettlementRepository settlementRepo;
    public InvestigationFullResponseDTO getInvestigationByClaimId(Long claimId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        Investigation inv = investigationRepo.findByClaim_ClaimId(claimId)
                .orElseThrow(() -> new RuntimeException("Investigation not found"));
        List<InvestigationNote> notes =
                noteRepo.findByInvestigation_InvestigationIdOrderByCreatedAtAsc(inv.getInvestigationId());
        return mapToFullDTO(inv, notes);
    }
    public InvUpdateResponseDTO  updateInvestigationAndCreateSettlement(Long investigationId, InvestigateUpdateStatusDTO dto) { //A
        Investigation inv = investigationRepo.findById(investigationId)
                .orElseThrow(() -> new RuntimeException("Investigation not found"));
        inv.setStatus(InvestigationStatus.valueOf(dto.getStatus()));
        inv.setOpenedAt(inv.getOpenedAt());
        Settlement settlement = null;
        if("CLOSED".equalsIgnoreCase(dto.getStatus())) {
            inv.setClosedAt(LocalDateTime.now());
            Claim claim = claimRepo.findById(inv.getClaim().getClaimId())
                    .orElseThrow(() -> new RuntimeException("Claim not found"));
            settlement = new Settlement();
            settlement.setClaim(claim);
            settlement.setRecommendedAmount(dto.getRecommendedAmount());
            settlement.setRecommendedBy(dto.getRecommendedBy());
            settlement.setRecommendedAt(LocalDateTime.now());
            settlement.setStatus(Status.IN_REVIEW);

            settlementRepo.save(settlement);
        }
        investigationRepo.save(inv);
        return mapToDTO(inv, settlement);
    }
    private InvestigationFullResponseDTO mapToFullDTO(Investigation inv, List<InvestigationNote> notes) { //A
        InvestigationFullResponseDTO dto = new InvestigationFullResponseDTO();
        dto.setInvestigationId(inv.getInvestigationId());
        dto.setStatus(inv.getStatus().name());
        dto.setOpenedAt(inv.getOpenedAt());
        dto.setClosedAt(inv.getClosedAt());
        List<InvestigationNoteDTO> noteDTOs = notes.stream()
                .map(n -> new InvestigationNoteDTO(
                        n.getNoteId(),
                        n.getAuthorId(),
                        n.getNoteText(),
                        n.getCreatedAt()
                ))
                .collect(Collectors.toList());
        dto.setInvestigationNotes(noteDTOs);
        return dto;
    }
    private InvUpdateResponseDTO  mapToDTO(Investigation inv, Settlement settlement) { //A
        InvUpdateResponseDTO  dto=new InvUpdateResponseDTO();
        dto.setInvestigationId(inv.getInvestigationId());
        dto.setStatus(inv.getStatus().name());
        dto.setClosedAt(inv.getClosedAt());

        if (settlement != null) {
            SettlementResponseDTO sDto = new SettlementResponseDTO();
            sDto.setSettlementId(settlement.getSettlementId());
            sDto.setClaimId(inv.getClaim().getClaimId());
            sDto.setRecommendedAt(settlement.getRecommendedAt());
            sDto.setRecommendedAmount(settlement.getRecommendedAmount());
            sDto.setRecommendedBy(settlement.getRecommendedBy());
            sDto.setStatus(settlement.getStatus().name());
            dto.setSettlement(sDto);
        }

        return dto;
    }
}
