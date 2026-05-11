package com.cts.identity.repository;

import com.cts.identity.entity.Claimant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimantRepository extends JpaRepository<Claimant, Long> {
    List<Claimant> findByUserId(String userId);
}
