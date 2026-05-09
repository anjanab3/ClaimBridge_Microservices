package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.Notification;
import com.cts.claimbridge.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    /** Policyholder notifications (numeric userId). */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserNotification(@PathVariable Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "5") int size
    ) {
        Page<Notification> notifications = notificationService.getUserNotifications(userId, page, size);
        if (notifications.isEmpty()) {
            return ResponseEntity.ok().body(new ResponseDTO("No Notifications Found"));
        }
        return ResponseEntity.ok().body(notifications);
    }

    /** Staff (adjuster / fraud analyst) notifications (string userId, e.g. "CA-0001"). */
    @GetMapping("/staff/{staffUserId}")
    public ResponseEntity<?> getStaffNotifications(@PathVariable String staffUserId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<Notification> notifications = notificationService.getStaffNotifications(staffUserId, page, size);
        if (notifications.isEmpty()) {
            return ResponseEntity.ok().body(new ResponseDTO("No Notifications Found"));
        }
        return ResponseEntity.ok().body(notifications);
    }

    // endpoint to mark a notification as read
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id){
        try {
            return ResponseEntity.ok().body(new MessageDTO(notificationService.markAsRead(id),"Notification updated successfully"));
        }
        catch(Exception e)
        {
            return ResponseEntity.badRequest().body(new ResponseDTO("User Id is incorrect"));
        }
    }

}
