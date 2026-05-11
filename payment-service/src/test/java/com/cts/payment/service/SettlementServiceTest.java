package com.cts.payment.service;

import com.cts.payment.client.ClaimsServiceClient;
import com.cts.payment.dto.SettlementSyncDTO;
import com.cts.payment.entity.Settlement;
import com.cts.payment.repository.SettlementRepository;
import com.cts.payment.util.SettlementStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettlementServiceTest {

    @Mock private SettlementRepository settlementRepository;
    @Mock private ClaimsServiceClient claimsServiceClient;

    @InjectMocks private SettlementService settlementService;

    private Settlement settlement;
    private SettlementSyncDTO dto;

    @BeforeEach
    void setUp() {
        settlement = new Settlement();
        settlement.setSettlementId(1L);
        settlement.setClaimId(100L);
        settlement.setRecommendedAmount(BigDecimal.valueOf(5000));
        settlement.setStatus(SettlementStatus.PENDING);

        dto = new SettlementSyncDTO();
        dto.setClaimId(100L);
        dto.setRecommendedAmount(BigDecimal.valueOf(5000));
        dto.setRecommendedBy("adjuster-001");
        dto.setRecommendedAt(LocalDateTime.now());
    }

    @Test
    void getAllSettlements_ShouldReturnAllSettlements() {
        when(settlementRepository.findAll()).thenReturn(List.of(settlement));

        List<Settlement> result = settlementService.getAllSettlements();

        assertEquals(1, result.size());
        verify(settlementRepository).findAll();
    }

    @Test
    void getSettlementsByClaim_ShouldReturnSettlement_WhenFound() {
        when(settlementRepository.findByClaimId(100L)).thenReturn(Optional.of(settlement));

        Optional<Settlement> result = settlementService.getSettlementsByClaim(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getClaimId());
    }

    @Test
    void getSettlementsByClaim_ShouldReturnEmpty_WhenNotFound() {
        when(settlementRepository.findByClaimId(999L)).thenReturn(Optional.empty());

        Optional<Settlement> result = settlementService.getSettlementsByClaim(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateStatus_ShouldApproveSettlement() {
        when(settlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        settlementService.updateStatus(1L, "APPROVED");

        assertEquals(SettlementStatus.APPROVED, settlement.getStatus());
        verify(settlementRepository).save(settlement);
    }

    @Test
    void updateStatus_ShouldRejectSettlement() {
        when(settlementRepository.findById(1L)).thenReturn(Optional.of(settlement));

        settlementService.updateStatus(1L, "REJECTED");

        assertEquals(SettlementStatus.REJECTED, settlement.getStatus());
        verify(settlementRepository).save(settlement);
    }

    @Test
    void updateStatus_ShouldThrow_WhenSettlementNotFound() {
        when(settlementRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> settlementService.updateStatus(99L, "APPROVED"));
    }

    @Test
    void receiveSettlement_ShouldSaveNew_WhenNoDuplicateExists() {
        when(settlementRepository.findByClaimId(100L)).thenReturn(Optional.empty());
        when(settlementRepository.save(any(Settlement.class))).thenReturn(settlement);

        Settlement result = settlementService.receiveSettlement(dto);

        assertNotNull(result);
        verify(settlementRepository).save(any(Settlement.class));
    }

    @Test
    void receiveSettlement_ShouldReturnExisting_WhenDuplicateExists() {
        when(settlementRepository.findByClaimId(100L)).thenReturn(Optional.of(settlement));

        Settlement result = settlementService.receiveSettlement(dto);

        assertEquals(settlement.getSettlementId(), result.getSettlementId());
        // Should NOT call save again for a duplicate
        verify(settlementRepository, never()).save(any());
    }
}
