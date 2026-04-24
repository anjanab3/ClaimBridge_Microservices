package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.PolicyHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyHolderRepository extends JpaRepository<PolicyHolder,Long> {
    PolicyHolder findByPolicy_PolicyId(Long policyId); //A
}
