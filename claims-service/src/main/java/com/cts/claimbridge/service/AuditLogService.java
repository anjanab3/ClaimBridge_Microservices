package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.AuditLog;
import com.cts.claimbridge.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Records an audit entry for any action performed by a user.
     *
     * @param userId   ID of the user performing the action
     * @param action   Action type — e.g., "CREATE", "UPDATE", "DELETE", "LOGIN", "STATUS_CHANGE"
     * @param resource Resource affected — e.g., "CLAIM", "POLICY", "FRAUD_ALERT", "SETTLEMENT"
     * @param details  Optional key-value map of extra context (e.g., claimId, oldStatus, newStatus)
     */
    public void logAction(Long userId, String action, String resource, Map<String, Object> details) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action.toUpperCase());
        log.setResource(resource.toUpperCase());
        log.setTimestamp(LocalDateTime.now());
        log.setDetails(details);
        auditLogRepository.save(log);
    }

    // Convenience overload — log without extra details.
    public void logAction(Long userId, String action, String resource) {
        logAction(userId, action, resource, null);
    }

}
