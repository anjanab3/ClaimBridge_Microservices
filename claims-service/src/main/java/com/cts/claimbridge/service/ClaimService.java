package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.ClaimStatusDTO;
import com.cts.claimbridge.dto.TriageDecisionRequestDTO;
import com.cts.claimbridge.dto.TriageRequestDTO;
import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.*;
import com.cts.claimbridge.util.ClaimStatus;
import com.cts.claimbridge.util.PaymentStatus;
import com.cts.claimbridge.util.Status;
import com.cts.claimbridge.util.TriageStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClaimService {

    @Autowired
    private  ClaimRepository claimRepository;
    @Autowired
    private PolicyRepository policyRepository;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private EvidenceRepository evidenceRepository;
    @Autowired
    private FraudScoringService fraudScoringService;
    // @Autowired
    // private ReportingService reportingService;

    public Claim save(Claim claim ,long policyId , long holderId) {

        Policy policy = policyRepository.findById(policyId).orElseThrow(() -> new RuntimeException("Policy Not found"));
        claim.setPolicy(policy);
//        SLA sla=slaRepository
//                .findByMonitoredEntityAndActiveTrue("CLAIM")
//                .orElseThrow(()->new RuntimeException("SLA Not configures"));
//        claim.setSla(sla);

        //set created date
//        claim.setCreatedDate(LocalDateTime.now());
//
//        //calculate response due date
//        claim.setResponseDueDate(
//                claim.getCreatedDate().plusHours(sla.getResponseHours())
//        );
//        //calculate resolution due date
//        claim.setResolutionDueDate(
//                claim.getCreatedDate().plusHours(sla.getResolutionHours())
//        );
        //save claim
        Claim savedClaim= claimRepository.save(claim);
        // auto-generate fraud score immediately after submission
        fraudScoringService.scoreAndPersist(savedClaim);
        //notificate as claim created
        notificationService.sendNotification(
                holderId,savedClaim.getClaimId(),
                "Your claim has been successfully submitted",
                "Intake"
        );

        return savedClaim;
    }

    public Claim updateClaimStatus(Long claimId, ClaimStatus status) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim Not Found"));
        String oldStatus = claim.getStatus() != null ? claim.getStatus().name() : "NONE";
        claim.setStatus(status);
        Claim updatedClaim = claimRepository.save(claim);

        // auto-store report on status change
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("claimId", claimId);
        params.put("oldStatus", oldStatus);
        params.put("newStatus", status.name());
        params.put("changedAt", LocalDateTime.now().toString());
       // reportingService.recordEvent("CLAIM_STATUS_CHANGE", params);

        // Send Notification after status update
        Long holderId = claim.getPolicy().getHolder().getHolderId();
        notificationService.sendNotification(
                holderId,
                claimId,
                "Your claim status has been updated to " + status,
                "CLAIM"
        );
        return updatedClaim;
    }

    public List<Claim> findByHolder(Long holderId) {
        return claimRepository.findByPolicy_Holder_HolderId(holderId);
    }

    public Page<Claim> findAllClaims(int page , int size) {
        Pageable pageable = PageRequest.of(page , size);
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
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + claimId));

        String oldStatus = claim.getStatus() != null ? claim.getStatus().name() : "NONE";
        claim.setStatus(newStatus);
        Claim saved = claimRepository.save(claim);

        // auto-store report on validation status change
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("claimId", claimId);
        params.put("oldStatus", oldStatus);
        params.put("newStatus", newStatus.name());
        params.put("changedAt", LocalDateTime.now().toString());
        //reportingService.recordEvent("CLAIM_STATUS_CHANGE", params);

        return saved;
    }

    @Transactional
    public Claim triageClaim(Long claimId, TriageRequestDTO triageRequest) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with ID: " + claimId));
        String oldStatus = claim.getStatus() != null ? claim.getStatus().name() : "NONE";
        if (triageRequest.getStatus() != null) {
            claim.setStatus(ClaimStatus.valueOf(triageRequest.getStatus().name()));
        }
        if (claim.getEstimatedAmount() > 50000) {
            claim.setStatus(ClaimStatus.IN_REVIEW);
        }

        Claim saved = claimRepository.save(claim);

        // auto-store report on triage status change
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("claimId", claimId);
        params.put("oldStatus", oldStatus);
        params.put("newStatus", saved.getStatus() != null ? saved.getStatus().name() : "NONE");
        params.put("changedAt", LocalDateTime.now().toString());
        //reportingService.recordEvent("CLAIM_STATUS_CHANGE", params);

        return saved;
    }

    public Claim updateStatus(Long claimId, ClaimStatus status) {
        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
        String oldStatus = claim.getStatus() != null ? claim.getStatus().name() : "NONE";
        claim.setStatus(status);
        Claim saved = claimRepository.save(claim);

        // auto-store report on status change
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("claimId", claimId);
        params.put("oldStatus", oldStatus);
        params.put("newStatus", status.name());
        params.put("changedAt", LocalDateTime.now().toString());
        //reportingService.recordEvent("CLAIM_STATUS_CHANGE", params);

        return saved;
    }

    public Page<Claim> findByStatus(ClaimStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRepository.findByStatus(status, pageable);
    }

}
