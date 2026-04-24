package com.cts.claimbridge.repository;

import com.cts.claimbridge.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find all audit logs for a specific user
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    // Find all audit logs for a specific resource (e.g., "Claim", "FraudAlert", "Policy")
    Page<AuditLog> findByResource(String resource, Pageable pageable);

    // Find all audit logs for a specific action
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Find all audit logs within a specific date range
    Page<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    
}
