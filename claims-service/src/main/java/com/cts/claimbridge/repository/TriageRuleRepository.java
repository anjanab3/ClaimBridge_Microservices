package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.TriageRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TriageRuleRepository extends JpaRepository<TriageRule, Long> {

    // Get all active or inactive rules
    List<TriageRule> findByActive(Boolean active);

    // Filter rules by assigned queue (ADJUSTER or FRAUD)
    List<TriageRule> findByAssignedQueue(String assignedQueue);

    // Filter rules by priority level
    List<TriageRule> findByPriority(String priority);

    // Get all active non-default rules (used for matching)
    List<TriageRule> findByActiveAndIsDefault(Boolean active, Boolean isDefault);

    // Get the fallback default rule
    Optional<TriageRule> findByIsDefaultTrue();

}
