package com.cts.claimbridge.repository;
import com.cts.claimbridge.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByClaim_ClaimId(Long claimId);
}

