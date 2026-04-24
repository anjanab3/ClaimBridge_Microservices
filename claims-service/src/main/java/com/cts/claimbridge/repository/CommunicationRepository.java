package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.Communication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CommunicationRepository extends JpaRepository<Communication,Long> {
    List<Communication> findByClaim_ClaimIdOrderBySentAtAsc(Long claimId); //A
    List<Communication> findByClaim_ClaimIdOrderBySentAtDesc(Long claimId);

    Page<Communication> findByToUserId(long userId, Pageable pageable);
    Page<Communication> findByClaim_ClaimIdOrderBySentAtDesc(Long claimId, Pageable pageable);
}
