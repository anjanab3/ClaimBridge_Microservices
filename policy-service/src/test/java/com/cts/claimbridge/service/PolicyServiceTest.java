package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.repository.PolicyRepository;
import com.cts.claimbridge.util.PolicyStatus;
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
class PolicyServiceTest {

    @Mock private PolicyRepository policyRepo;

    @InjectMocks private PolicyService policyService;

    private Policy policy;

    @BeforeEach
    void setUp() {
        policy = new Policy();
        policy.setPolicyId(1L);
        policy.setPolicyNumber("POL-001");
        policy.setStatus(PolicyStatus.ACTIVE);
        policy.setHolderId(10L);
    }

    @Test
    void findById_ShouldReturnPolicy_WhenFound() {
        when(policyRepo.findById(1L)).thenReturn(Optional.of(policy));

        Optional<Policy> result = policyService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("POL-001", result.get().getPolicyNumber());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotFound() {
        when(policyRepo.findById(99L)).thenReturn(Optional.empty());

        Optional<Policy> result = policyService.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByNumber_ShouldReturnPolicy_WhenFound() {
        when(policyRepo.findByPolicyNumber("POL-001")).thenReturn(Optional.of(policy));

        Optional<Policy> result = policyService.findByNumber("POL-001");

        assertTrue(result.isPresent());
    }

    @Test
    void savePolicies_ShouldSaveAll_WhenNoDuplicates() {
        when(policyRepo.existsByPolicyNumber("POL-001")).thenReturn(false);
        when(policyRepo.saveAll(anyList())).thenReturn(List.of(policy));

        List<Policy> result = policyService.savePolicies(List.of(policy));

        assertEquals(1, result.size());
        verify(policyRepo).saveAll(anyList());
    }

    @Test
    void savePolicies_ShouldThrow_WhenDuplicatePolicyNumber() {
        when(policyRepo.existsByPolicyNumber("POL-001")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> policyService.savePolicies(List.of(policy)));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void findPolicyIdsByHolderId_ShouldReturnIds() {
        when(policyRepo.findByHolderId(10L)).thenReturn(List.of(policy));

        List<Long> ids = policyService.findPolicyIdsByHolderId(10L);

        assertEquals(1, ids.size());
        assertEquals(1L, ids.get(0));
    }

    @Test
    void updateCoverage_ShouldUpdateAndSave() {
        when(policyRepo.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepo.save(any(Policy.class))).thenReturn(policy);

        Policy result = policyService.updateCoverage(1L, "{\"type\":\"Fire\"}");

        assertNotNull(result);
        assertEquals("{\"type\":\"Fire\"}", result.getCoverageJSON());
        verify(policyRepo).save(policy);
    }

    @Test
    void updateCoverage_ShouldThrow_WhenPolicyNotFound() {
        when(policyRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> policyService.updateCoverage(99L, "{}"));
    }

    @Test
    void updateStatus_ShouldChangeStatus() {
        when(policyRepo.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepo.save(any(Policy.class))).thenReturn(policy);

        Policy result = policyService.updateStatus(1L, "SUSPENDED");

        assertNotNull(result);
        verify(policyRepo).save(policy);
    }
}
