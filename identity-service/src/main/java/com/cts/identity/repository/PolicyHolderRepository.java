package com.cts.identity.repository;

import com.cts.identity.entity.PolicyHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyHolderRepository extends JpaRepository<PolicyHolder, Long> {
}
