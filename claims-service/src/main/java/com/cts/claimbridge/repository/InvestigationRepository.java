package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.Investigation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    Optional<Investigation> findByClaim_ClaimId(Long claimId);//A
}
