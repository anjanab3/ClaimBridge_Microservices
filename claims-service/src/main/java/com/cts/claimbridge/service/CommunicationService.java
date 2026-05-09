package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.Communication;
import com.cts.claimbridge.entity.Notification;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.CommunicationRepository;
import com.cts.claimbridge.repository.NotificationRepository;
import com.cts.claimbridge.repository.TriageDecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CommunicationService {

    @Autowired
    private CommunicationRepository communicationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private TriageDecisionRepository triageDecisionRepository;

    // ── Add a communication message ──────────────────────────────────────────
    public Communication addCommunication(Long claimId, Communication comm) {
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found: " + claimId));
        comm.setClaim(claim);

        if (comm.getSentAt() == null) {
            comm.setSentAt(LocalDateTime.now());
        }
        if (comm.getDirection() == null) {
            comm.setDirection(comm.getToUserId() == null || comm.getToUserId().isBlank()
                    ? "INTERNAL" : "OUTBOUND");
        }

        Communication saved = communicationRepository.save(comm);

        // Auto-create Notification for policyholder when adjuster sends OUTBOUND
        if ("OUTBOUND".equalsIgnoreCase(saved.getDirection())
                && saved.getToUserId() != null && !saved.getToUserId().isBlank()) {
            try {
                Long holderId = Long.parseLong(saved.getToUserId());
                Notification notification = new Notification();
                notification.setUserId(holderId);
                notification.setClaim(claim);
                notification.setMessage("New message from your adjuster regarding claim CLM-" + claimId);
                notification.setCategory("MESSAGE");
                notification.setStatus("UNREAD");
                notification.setCreatedAt(LocalDateTime.now());
                notificationRepository.save(notification);
            } catch (NumberFormatException ignored) {
                // toUserId was not a numeric holderId — skip notification
            }
        }

        // For INBOUND (policyholder → adjuster): ensure toUserId is set to the assigned adjuster,
        // then fire a staff notification so the adjuster sees it in their bell.
        if ("INBOUND".equalsIgnoreCase(saved.getDirection())) {
            String adjusterUserId = saved.getToUserId();

            // If toUserId is blank, look up the assigned adjuster from the latest triage decision
            if (adjusterUserId == null || adjusterUserId.isBlank()) {
                adjusterUserId = triageDecisionRepository
                        .findTopByClaimIdOrderByAssignedAtDesc(claimId)
                        .map(td -> td.getAssignedTo())
                        .orElse(null);
                if (adjusterUserId != null && !adjusterUserId.isBlank()) {
                    saved.setToUserId(adjusterUserId);
                    communicationRepository.save(saved);
                }
            }

            // Notify the adjuster / fraud analyst
            if (adjusterUserId != null && !adjusterUserId.isBlank()) {
                try {
                    Notification staffNotif = new Notification();
                    staffNotif.setStaffUserId(adjusterUserId);
                    staffNotif.setClaim(claim);
                    staffNotif.setMessage("New message from policyholder on claim CLM-" + claimId);
                    staffNotif.setCategory("MESSAGE");
                    staffNotif.setStatus("UNREAD");
                    staffNotif.setCreatedAt(LocalDateTime.now());
                    notificationRepository.save(staffNotif);
                } catch (Exception ignored) { /* non-fatal */ }
            }
        }

        return saved;
    }

    // ── Mark a single communication as read ──────────────────────────────────
    public Communication markAsRead(Long commId) {
        Communication comm = communicationRepository.findById(commId)
                .orElseThrow(() -> new RuntimeException("Communication not found: " + commId));
        comm.setRead(true);
        return communicationRepository.save(comm);
    }

    // ── Mark all communications sent to a user for a claim as read ───────────
    public void markAllReadForClaim(Long claimId, String userId) {
        communicationRepository
                .findByClaim_ClaimIdAndToUserIdOrderBySentAtAsc(claimId, userId)
                .forEach(c -> {
                    if (!c.isRead()) {
                        c.setRead(true);
                        communicationRepository.save(c);
                    }
                });
    }

    // ── Paginated messages for a claim ───────────────────────────────────────
    public Page<Communication> getCommunicationsByClaim(Long claimId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return communicationRepository.findByClaim_ClaimIdOrderBySentAtDesc(claimId, pageable);
    }

    // ── Paginated messages by recipient userId ────────────────────────────────
    public Page<Communication> getCommunicationsByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return communicationRepository.findByToUserId(userId, pageable);
    }

    // ── Count unread messages for a recipient ─────────────────────────────────
    public long countUnreadByUserId(String userId) {
        return communicationRepository.countByToUserIdAndIsRead(userId, false);
    }

    // ── Notifications by claim ────────────────────────────────────────────────
    public List<Notification> getNotificationsByClaim(Long claimId) {
        return notificationRepository.findByClaim_ClaimIdOrderByCreatedAtDesc(claimId);
    }
}
