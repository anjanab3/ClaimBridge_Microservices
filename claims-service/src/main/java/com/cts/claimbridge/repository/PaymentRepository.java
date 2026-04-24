package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySettlement_SettlementId(Long settlementId);
    List<Payment> findByPaymentIdAndStatus(Long paymentId, String status);
   // boolean existsBySettlementId(Long settlementId);
   Optional<Payment> findBySettlementSettlementId(Long settlementId);
}

