package com.cts.identity.repository;

import com.cts.identity.entity.TriageDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TriageDecisionRepository extends JpaRepository<TriageDecision, Long> {
    List<TriageDecision> findByClaimId(Long claimId);
    List<TriageDecision> findByAssignedTo(String assignedTo);
    Optional<TriageDecision> findTopByClaimIdOrderByAssignedAtDesc(Long claimId);
    boolean existsByAssignedToAndClaimId(String assignedTo, Long claimId);
}
