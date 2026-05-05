package com.cts.payment.repository;

import com.cts.payment.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    Optional<Settlement> findByClaimId(Long claimId);
    boolean existsBySettlementId(Long settlementId);
}
