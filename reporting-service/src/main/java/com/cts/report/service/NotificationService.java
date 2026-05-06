package com.cts.report.service;

import com.cts.report.dto.NotificationRequestDTO;
import com.cts.report.dto.NotificationResponseDTO;
import com.cts.report.entity.Notification;
import com.cts.report.repository.NotificationRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // GET /{userId} — paged notifications for a user
    public Page<Notification> getUserNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationRepository.findByUserId(userId, pageable);
    }

    // PUT /{id}/read — mark a notification as read
    public String markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found: " + id));
        notification.setStatus("READ");
        notificationRepository.save(notification);
        return "Notification " + id + " marked as READ";
    }

    // POST /send — admin manually sends a notification
    public NotificationResponseDTO sendNotification(NotificationRequestDTO request) {
        validateCategory(request.getCategory());

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setClaimId(request.getClaimId());
        notification.setMessage(request.getMessage());
        notification.setCategory(request.getCategory().toUpperCase());
        notification.setStatus("UNREAD");
        notification.setCreatedAt(LocalDateTime.now());

        return toDTO(notificationRepository.save(notification));
    }

    // Internal — called by InternalEventController and SLAService
    public void createNotification(Long userId, Long claimId, String message, String category) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setClaimId(claimId);
        notification.setMessage(message);
        notification.setCategory(category);
        notification.setStatus("UNREAD");
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    // Helpers — validation and DTO conversion

    private void validateCategory(String category) {
        if (category == null || category.isBlank())
            throw new IllegalArgumentException("category is required. Allowed: INTAKE, INVESTIGATION, PAYMENT, FRAUD");
        switch (category.toUpperCase()) {
            case "INTAKE", "INVESTIGATION", "PAYMENT", "FRAUD" -> { /* valid */ }
            default -> throw new IllegalArgumentException(
                    "Invalid category: " + category + ". Allowed: INTAKE, INVESTIGATION, PAYMENT, FRAUD");
        }
    }

    private NotificationResponseDTO toDTO(Notification n) {
        return NotificationResponseDTO.builder()
                .notificationId(n.getNotificationId())
                .userId(n.getUserId())
                .claimId(n.getClaimId())
                .message(n.getMessage())
                .category(n.getCategory())
                .status(n.getStatus())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
