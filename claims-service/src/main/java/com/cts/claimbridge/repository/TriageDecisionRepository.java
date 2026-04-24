package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.TriageDecision;
import com.cts.claimbridge.util.Priority;
import com.cts.claimbridge.util.TriageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TriageDecisionRepository extends JpaRepository<TriageDecision, Long> {

    // All decisions for a specific claim
    List<TriageDecision> findByClaim_ClaimId(Long claimId);

//     // Most recent decision for a claim
//     Optional<TriageDecision> findTopByClaimIdOrderByAssignedAtDesc(Long claimId);

//     // All decisions assigned to a specific adjuster or queue
    List<TriageDecision> findByAssignedTo(String assignedTo);

//     // Check if a claim is assigned to an adjuster
    boolean existsByAssignedToAndClaim_ClaimId(String assignedTo, Long claimId);

//     // Filter decisions by priority level
//     List<TriageDecision> findByPriority(Priority priority);

//     // Returns only claims whose LATEST decision is in the given queue (non-paginated, internal use)
//     @Query("SELECT d FROM TriageDecision d WHERE d.assignedQueue = :assignedQueue AND d.assignedAt = " +
//            "(SELECT MAX(d2.assignedAt) FROM TriageDecision d2 WHERE d2.claimId = d.claimId)")
//     List<TriageDecision> findLatestByAssignedQueue(@Param("assignedQueue") String assignedQueue);

//     // Paginated version — used by controllers
//     @Query("SELECT d FROM TriageDecision d WHERE d.assignedQueue = :assignedQueue AND d.assignedAt = " +
//            "(SELECT MAX(d2.assignedAt) FROM TriageDecision d2 WHERE d2.claimId = d.claimId)")
//     Page<TriageDecision> findLatestByAssignedQueue(@Param("assignedQueue") String assignedQueue, Pageable pageable);
 }
