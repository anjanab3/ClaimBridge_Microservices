package com.cts.identity.repository;

import com.cts.identity.entity.TriageRule;
import com.cts.identity.util.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TriageRuleRepository extends JpaRepository<TriageRule, Long> {
    List<TriageRule> findByActive(Boolean active);
    List<TriageRule> findByAssignedQueue(String assignedQueue);
    List<TriageRule> findByPriority(Priority priority);
    Optional<TriageRule> findByIsDefaultTrue();
}
