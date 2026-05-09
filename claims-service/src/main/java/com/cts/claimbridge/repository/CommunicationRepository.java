package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.Communication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunicationRepository extends JpaRepository<Communication, Long> {

    List<Communication> findByClaim_ClaimIdOrderBySentAtAsc(Long claimId);
    List<Communication> findByClaim_ClaimIdOrderBySentAtDesc(Long claimId);
    List<Communication> findByClaim_ClaimIdAndToUserIdOrderBySentAtAsc(Long claimId, String toUserId);

    Page<Communication> findByToUserId(String userId, Pageable pageable);
    Page<Communication> findByClaim_ClaimIdOrderBySentAtDesc(Long claimId, Pageable pageable);

    long countByToUserIdAndIsRead(String userId, boolean isRead);
}
