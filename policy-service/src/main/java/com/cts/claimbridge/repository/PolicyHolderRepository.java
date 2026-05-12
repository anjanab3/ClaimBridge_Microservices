package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.PolicyHolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PolicyHolderRepository extends JpaRepository<PolicyHolder,Long> {
    Optional<PolicyHolder> findByTaxID(String taxId);
    Optional<PolicyHolder> findByName(String name);
}
