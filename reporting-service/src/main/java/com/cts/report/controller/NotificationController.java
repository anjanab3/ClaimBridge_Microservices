package com.cts.report.controller;

import com.cts.report.dto.MessageDTO;
import com.cts.report.dto.NotificationRequestDTO;
import com.cts.report.dto.NotificationResponseDTO;
import com.cts.report.dto.ResponseDTO;
import com.cts.report.entity.Notification;
import com.cts.report.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    // GET /{userId} — paged notifications for a user (any authenticated role)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserNotification(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<Notification> notifications = notificationService.getUserNotifications(userId, page, size);

        if (notifications.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Notifications Found"));

        return ResponseEntity.ok(notifications);
    }

    // PUT /{id}/read — mark a notification as read (any authenticated role)
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(
                    new MessageDTO(notificationService.markAsRead(id), "Notification updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDTO("Notification ID is incorrect"));
        }
    }

    // POST /send — admin manually sends a notification
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDTO> sendNotification(@RequestBody NotificationRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificationService.sendNotification(request));
    }
}
