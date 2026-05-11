package com.cts.claimbridge.service;

import com.cts.claimbridge.client.PaymentServiceClient;
import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.*;
import com.cts.claimbridge.util.InvestigationStatus;
import com.cts.claimbridge.util.Status;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Transactional
    public InvUpdateResponseDTO updateInvestigationAndCreateSettlement(
            Long investigationId, InvestigateUpdateStatusDTO dto) {

        Investigation inv = investigationRepo.findById(investigationId)
                .orElseThrow(() -> new RuntimeException("Investigation not found"));

        // If already closed — ensure settlement was still created (catches cases where
        // the investigation was closed without going through the settlement flow)
        if (inv.getStatus() == InvestigationStatus.CLOSED) {
            Claim closedClaim = claimRepo.findById(inv.getClaim().getClaimId())
                    .orElseThrow(() -> new RuntimeException("Claim not found"));
            List<Settlement> existingList = settlementRepo.findByClaim_ClaimId(closedClaim.getClaimId());
            if (!existingList.isEmpty()) {
                // Settlement exists in claims DB — push to payment-service in case it never arrived
                Settlement existing = existingList.get(0);
                sendSettlementToPayment(existing);
                return mapToDTO(inv, existing);
            }
            // No settlement at all — create and send
            String recommendedBy = SecurityContextHolder.getContext()
                    .getAuthentication().getName();
            Settlement s = new Settlement();
            s.setClaim(closedClaim);
            s.setRecommendedAmount(dto.getRecommendedAmount());
            s.setRecommendedBy(recommendedBy);
            s.setRecommendedAt(LocalDateTime.now());
            s.setStatus(Status.IN_REVIEW);
            Settlement saved = settlementRepo.save(s);
            sendSettlementToPayment(saved);
            return mapToDTO(inv, saved);
        }

        inv.setStatus(InvestigationStatus.valueOf(dto.getStatus()));

        Settlement settlement = null;

        if ("CLOSED".equalsIgnoreCase(dto.getStatus())) {
            inv.setClosedAt(LocalDateTime.now());

            Claim claim = claimRepo.findById(inv.getClaim().getClaimId())
                    .orElseThrow(() -> new RuntimeException("Claim not found"));

            // Get recommendedBy from JWT — no need for frontend to send it
            String recommendedBy = SecurityContextHolder.getContext()
                    .getAuthentication().getName();

            List<Settlement> existing = settlementRepo.findByClaim_ClaimId(claim.getClaimId());
            if (!existing.isEmpty()) {
                // Settlement already in claims DB — push to payment-service in case it missed it
                settlement = existing.get(0);
                sendSettlementToPayment(settlement);
            } else {
                settlement = new Settlement();
                settlement.setClaim(claim);
                settlement.setRecommendedAmount(dto.getRecommendedAmount());
                settlement.setRecommendedBy(recommendedBy);
                settlement.setRecommendedAt(LocalDateTime.now());
                settlement.setStatus(Status.IN_REVIEW);

                Settlement savedSettlement = settlementRepo.save(settlement);

                // Push settlement to payment-service via Feign
                sendSettlementToPayment(savedSettlement);

                settlement = savedSettlement;
            }
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
            sDto.setClaimId(settlement.getClaim() != null ? settlement.getClaim().getClaimId() : null);
            sDto.setRecommendedAt(settlement.getRecommendedAt());
            sDto.setRecommendedAmount(settlement.getRecommendedAmount());
            sDto.setRecommendedBy(settlement.getRecommendedBy());
            sDto.setStatus(settlement.getStatus().name());
            dto.setSettlement(sDto);
        }

        return dto;
    }
}
