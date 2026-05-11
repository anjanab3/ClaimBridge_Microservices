package com.cts.claimbridge.service;

import com.cts.claimbridge.client.PolicyServiceClient;
import com.cts.claimbridge.dto.PolicyDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.EvidenceRepository;
import com.cts.claimbridge.util.ClaimStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private EvidenceRepository evidenceRepository;
    @Mock private NotificationService notificationService;
    @Mock private FraudScoringService fraudScoringService;
    @Mock private PolicyServiceClient policyServiceClient;

    @InjectMocks private ClaimService claimService;

    private Claim claim;
    private PolicyDTO policy;

    @BeforeEach
    void setUp() {
        claim = new Claim();
        claim.setClaimId(1L);
        claim.setStatus(ClaimStatus.IN_COMING);

        policy = new PolicyDTO();
        policy.setHolderId(10L);
    }

    @Test
    void save_ShouldSaveClaim_WhenPolicyExistsAndNoDuplicate() {
        when(policyServiceClient.getPolicyById(1L)).thenReturn(policy);
        when(claimRepository.existsByPolicyIdAndStatusIn(eq(1L), anyList())).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        Claim result = claimService.save(claim, 1L, 10L);

        assertNotNull(result);
        verify(claimRepository).save(claim);
        verify(fraudScoringService).scoreAndPersist(claim);
    }

    @Test
    void save_ShouldThrow_WhenPolicyNotFound() {
        when(policyServiceClient.getPolicyById(99L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.save(claim, 99L, 10L));

        assertTrue(ex.getMessage().contains("Policy not found"));
    }

    @Test
    void save_ShouldThrow_WhenDuplicateActiveClaim() {
        when(policyServiceClient.getPolicyById(1L)).thenReturn(policy);
        when(claimRepository.existsByPolicyIdAndStatusIn(eq(1L), anyList())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> claimService.save(claim, 1L, 10L));

        assertTrue(ex.getMessage().contains("already in progress"));
    }

    @Test
    void findById_ShouldReturnClaim_WhenFound() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));

        Optional<Claim> result = claimService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getClaimId());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Claim> result = claimService.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void updateClaimStatus_ShouldUpdateAndSave() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);
        when(policyServiceClient.getPolicyById(any())).thenReturn(policy);

        Claim result = claimService.updateClaimStatus(1L, ClaimStatus.IN_REVIEW);

        assertNotNull(result);
        verify(claimRepository).save(claim);
    }

    @Test
    void updateClaimStatus_ShouldThrow_WhenClaimNotFound() {
        when(claimRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> claimService.updateClaimStatus(99L, ClaimStatus.IN_REVIEW));
    }

    @Test
    void validateClaim_ShouldUpdateStatus() {
        when(claimRepository.findById(1L)).thenReturn(Optional.of(claim));
        when(claimRepository.save(any(Claim.class))).thenReturn(claim);

        Claim result = claimService.validateClaim(1L, ClaimStatus.VALID);

        assertNotNull(result);
        verify(claimRepository).save(claim);
    }

    @Test
    void findAll_ShouldReturnAllClaims() {
        when(claimRepository.findAll()).thenReturn(List.of(claim));

        List<Claim> result = claimService.findAll();

        assertEquals(1, result.size());
    }
}
