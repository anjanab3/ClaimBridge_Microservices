package com.cts.payment.repository;

import com.cts.payment.entity.Payment;
import com.cts.payment.util.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findBySettlementId(Long settlementId);

    Optional<Payment> findByClaimId(Long claimId);

    boolean existsBySettlementId(Long settlementId);

    Optional<Payment> findByPaymentIdAndStatus(Long paymentId, PaymentStatus status);

    List<Payment> findByStatus(PaymentStatus status);
}
