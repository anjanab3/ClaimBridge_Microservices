package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.Communication;
import com.cts.claimbridge.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByClaim_ClaimIdOrderByCreatedAtDesc(Long claimID);

    Page<Notification> findByUserId(Long userId, Pageable pageable);
    List<Notification>findByStatus(String status);
}
