package com.cts.claimbridge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cts.claimbridge.entity.Evidence;

@Repository
public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    List<Evidence> findByClaim_ClaimId(Long id);

    @Query("SELECT e.verified FROM Evidence e WHERE e.claim.claimId = :claimId")
    Optional<Boolean> findVerifiedByClaimId(@Param("claimId") Long claimId);

    //  @Query("SELECT e FROM Evidence e JOIN e.claim c JOIN c.policy p JOIN p.holder h " +
    //         "WHERE h.holderId = :holderId AND c.claimId = :claimId AND e.evidenceId = :evidenceId")
    // Optional<Evidence> findByHolderAndClaimAndEvidenceId(long holderId, long claimId, long evidenceId);

    // List<Evidence> findByClaim_ClaimId(Long id);

    // @Query("SELECT e.verified FROM Evidence e WHERE e.claim.claimId = :claimId")
    // Optional<Boolean> findVerifiedByClaimId(@Param("claimId") Long claimId);
}