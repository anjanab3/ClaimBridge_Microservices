package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.FraudAlert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, Long> {

    // Find alert by the associated alert ID
    Optional<FraudAlert> findByAlertId(Long alertId);

    // Filter alerts by status (e.g., OPEN, ESCALATED)
    Page<FraudAlert> findByStatus(String status, Pageable pageable);

    // Filter alerts by multiple statuses (used to return only active alerts)
    List<FraudAlert> findByStatusIn(List<String> statuses);

    // Paginated version — used by controllers
    Page<FraudAlert> findByStatusIn(List<String> statuses, Pageable pageable);
    
    // Find all alerts by the associated Claim ID
    List<FraudAlert> findByClaim_ClaimId(Long claimId);
    
}