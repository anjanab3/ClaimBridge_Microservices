package com.cts.payment.repository;

import com.cts.payment.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    // Plain column query — replaces findByClaim_ClaimId from claims-service
    List<Settlement> findByClaimId(Long claimId);
}
