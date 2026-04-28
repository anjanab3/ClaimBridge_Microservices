package com.cts.identity.repository;

import com.cts.identity.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);
    List<Policy> findByHolder_HolderId(Long holderId);
    boolean existsByPolicyNumber(String policyNumber);
}
