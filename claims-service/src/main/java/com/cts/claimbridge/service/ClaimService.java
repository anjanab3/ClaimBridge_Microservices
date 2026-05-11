package com.cts.claimbridge.service;

import com.cts.claimbridge.client.PolicyServiceClient;
import com.cts.claimbridge.dto.ClaimStatusDTO;
import com.cts.claimbridge.dto.PolicyDTO;
import com.cts.claimbridge.dto.TriageRequestDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.EvidenceRepository;
import com.cts.claimbridge.util.ClaimStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EvidenceRepository evidenceRepository;
    @Autowired
    private FraudScoringService fraudScoringService;
    @Autowired
    private PolicyServiceClient policyServiceClient;

    public Claim save(Claim claim, long policyId, long holderId) {
        log.info("Saving new claim for policyId={} holderId={}", policyId, holderId);
        PolicyDTO policy = fetchPolicy(policyId);
        if (policy == null) {
            log.warn("Policy not found for policyId={}", policyId);
            throw new RuntimeException("Policy not found with ID: " + policyId);
        }

        // Block duplicate active claims for the same policy
        boolean hasActiveClaim = claimRepository.existsByPolicyIdAndStatusIn(
                policyId, List.of(ClaimStatus.IN_COMING, ClaimStatus.IN_REVIEW));
        if (hasActiveClaim) {
            log.warn("Duplicate active claim detected for policyId={}", policyId);
            throw new RuntimeException(
                "A claim for this policy is already in progress. " +
                "You cannot raise another claim until the current one is resolved.");
        }

        claim.setPolicyId(policyId);
        claim.setHolderId(holderId);
        Claim savedClaim = claimRepository.save(claim);
        log.info("Claim saved with claimId={}", savedClaim.getClaimId());

        fraudScoringService.scoreAndPersist(savedClaim);

        notificationService.sendNotification(
                holderId,
                savedClaim.getClaimId(),
                "Your claim has been successfully submitted",
                "Intake"
        );

        return savedClaim;
    }

    public Claim updateClaimStatus(Long claimId, ClaimStatus status) {
        log.info("Updating status of claimId={} to {}", claimId, status);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim Not Found"));

        claim.setStatus(status);
        Claim updatedClaim = claimRepository.save(claim);

        Long holderId = getHolderIdFromPolicy(claim.getPolicyId());
        if (holderId != null) {
            notificationService.sendNotification(
                    holderId,
                    claimId,
                    "Your claim status has been updated to " + status,
                    "CLAIM"
            );
        }

        return updatedClaim;
    }

    public List<Claim> findByHolder(Long holderId) {
        List<Long> policyIds = fetchPolicyIdsByHolder(holderId);
        if (policyIds.isEmpty()) return List.of();
        return claimRepository.findByPolicyIdIn(policyIds);
    }

    public Page<Claim> findAllClaims(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.findAll(pageable);
    }

    public Optional<Claim> findById(Long id) {
        return claimRepository.findById(id);
    }

    public List<Claim> findAll() {
        return claimRepository.findAll();
    }

    public ClaimStatusDTO getClaimStatus(Long claimId) {
        ClaimStatus claimStatus = claimRepository.findClaimStatusById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found for id: " + claimId));

        Boolean evidenceVerified = evidenceRepository.findVerifiedByClaimId(claimId)
                .orElse(null);

        return new ClaimStatusDTO(
                claimStatus.name(),
                evidenceVerified == null ? "Evidence not yet submitted"
                        : evidenceVerified ? "Verified" : "Not Verified"
        );
    }

    public Page<Claim> getIncomingClaims(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.findByStatus(ClaimStatus.IN_COMING, pageable);
    }

    @Transactional
    public Claim validateClaim(Long claimId, ClaimStatus newStatus) {
        log.info("Validating claimId={} with status={}", claimId, newStatus);
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + claimId));

        claim.setStatus(newStatus);
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim triageClaim(Long claimId, TriageRequestDTO triageRequest) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + claimId));

        if (triageRequest.getStatus() != null) {
            claim.setStatus(ClaimStatus.valueOf(triageRequest.getStatus().name()));
        }
        if (claim.getEstimatedAmount() > 50000) {
            claim.setStatus(ClaimStatus.IN_REVIEW);
        }

        return claimRepository.save(claim);
    }

    public Claim updateStatus(Long claimId, ClaimStatus status) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        claim.setStatus(status);
        return claimRepository.save(claim);
    }

    public Page<Claim> findByStatus(ClaimStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.findByStatus(status, pageable);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private PolicyDTO fetchPolicy(Long policyId) {
        try {
            if (policyId == null) return null;
            return policyServiceClient.getPolicyById(policyId);
        } catch (Exception e) {
            return null;
        }
    }

    private Long getHolderIdFromPolicy(Long policyId) {
        PolicyDTO policy = fetchPolicy(policyId);
        return policy != null ? policy.getHolderId() : null;
    }

    private List<Long> fetchPolicyIdsByHolder(Long holderId) {
        try {
            return policyServiceClient.getPolicyIdsByHolderId(holderId);
        } catch (Exception e) {
            return List.of();
        }
    }
}
