package com.cts.claimbridge.service;

import com.cts.claimbridge.client.PaymentServiceClient;
import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.*;
import com.cts.claimbridge.util.InvestigationStatus;
import com.cts.claimbridge.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
    @Autowired
    private PaymentServiceClient paymentServiceClient;

    public InvestigationFullResponseDTO getInvestigationByClaimId(Long claimId) {
        claimRepo.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        Investigation inv = investigationRepo.findByClaim_ClaimId(claimId)
                .orElseThrow(() -> new RuntimeException("Investigation not found"));
        List<InvestigationNote> notes =
                noteRepo.findByInvestigation_InvestigationIdOrderByCreatedAtAsc(
                        inv.getInvestigationId());
        return mapToFullDTO(inv, notes);
    }

    public InvUpdateResponseDTO updateInvestigationAndCreateSettlement(
            Long investigationId, InvestigateUpdateStatusDTO dto) {

        Investigation inv = investigationRepo.findById(investigationId)
                .orElseThrow(() -> new RuntimeException("Investigation not found"));

        inv.setStatus(InvestigationStatus.valueOf(dto.getStatus()));

        Settlement settlement = null;

        if ("CLOSED".equalsIgnoreCase(dto.getStatus())) {
            inv.setClosedAt(LocalDateTime.now());

            Claim claim = claimRepo.findById(inv.getClaim().getClaimId())
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            settlement = new Settlement();
            settlement.setClaim(claim);
            settlement.setRecommendedAmount(dto.getRecommendedAmount());
            settlement.setRecommendedBy(dto.getRecommendedBy());
            settlement.setRecommendedAt(LocalDateTime.now());
            settlement.setStatus(Status.IN_REVIEW);

            Settlement savedSettlement = settlementRepo.save(settlement);

            // Push settlement to payment-service via Feign
            sendSettlementToPayment(savedSettlement);

            settlement = savedSettlement;
        }

        investigationRepo.save(inv);
        return mapToDTO(inv, settlement);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void sendSettlementToPayment(Settlement settlement) {
        try {
            SettlementSyncDTO dto = new SettlementSyncDTO();
            dto.setSettlementId(settlement.getSettlementId());
            dto.setClaimId(settlement.getClaim().getClaimId());
            dto.setRecommendedAmount(settlement.getRecommendedAmount());
            dto.setRecommendedBy(settlement.getRecommendedBy());
            dto.setRecommendedAt(settlement.getRecommendedAt());
            dto.setStatus(settlement.getStatus().name());
            paymentServiceClient.sendSettlementToPayment(dto);
            System.out.println("Settlement sent to payment-service for claimId="
                    + dto.getClaimId());
        } catch (Exception e) {
            System.err.println("Warning: could not send settlement to payment-service: "
                    + e.getMessage());
        }
    }

    private InvestigationFullResponseDTO mapToFullDTO(Investigation inv,
                                                       List<InvestigationNote> notes) {
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

    private InvUpdateResponseDTO mapToDTO(Investigation inv, Settlement settlement) {
        InvUpdateResponseDTO dto = new InvUpdateResponseDTO();
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