package com.cts.claimbridge.repository;

import com.cts.claimbridge.util.ClaimStatus;
import com.cts.claimbridge.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    // Replaces findByPolicy_Holder_HolderId — Claim now stores policyId as plain Long
    List<Claim> findByPolicyIdIn(List<Long> policyIds);

    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);

    Page<Claim> findByLossType(String lossType, Pageable pageable);

    @Query("SELECT c.status FROM Claim c WHERE c.claimId = :claimId")
    Optional<ClaimStatus> findClaimStatusById(@Param("claimId") Long claimId);

    long countByPolicyIdInAndCreatedDateAfter(List<Long> policyIds, LocalDateTime after);

}