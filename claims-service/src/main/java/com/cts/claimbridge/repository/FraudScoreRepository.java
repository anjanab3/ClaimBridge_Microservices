package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.FraudScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface FraudScoreRepository extends JpaRepository<FraudScore, Long> {

    // Retrieve the most recent score for a specific claim
   // Optional<FraudScore> findByClaimId(Long claimId);
    
    // Find score by its associated alert
    Optional<FraudScore> findByFraudAlertAlertId(Long alertId);

    Optional<FraudScore> findByClaim_ClaimId(Long claimId);
}