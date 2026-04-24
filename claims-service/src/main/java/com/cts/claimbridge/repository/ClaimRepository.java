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
public interface ClaimRepository extends JpaRepository<Claim,Long> {
    List<Claim> findByPolicy_Holder_HolderId(Long holderId);
    Page<Claim> findByStatus(ClaimStatus status, Pageable pageable);
//    List<Claim> findByResponseDueDateBefore(LocalDateTime time);
//    List<Claim> findByResolutionDueDateBefore(LocalDateTime time);
    Page<Claim> findByLossType(String losType, Pageable pageable);

    @Query("SELECT c.status FROM Claim c WHERE c.claimId = :claimId")
    Optional<ClaimStatus> findClaimStatusById(@Param("claimId") Long claimId);

    // Used by FraudScoringService to detect frequent claimants (>1 claim in 90 days).
    long countByPolicy_Holder_HolderIdAndCreatedDateAfter(Long holderId, LocalDateTime cutoff);

    @Query("SELECT c FROM Claim c " +
            "JOIN c.settlementList s " +
            "JOIN s.payments p " +
            "WHERE p.paymentId = :paymentId")
    Optional<Claim> findByPaymentId(@Param("paymentId") Long paymentId);
}
