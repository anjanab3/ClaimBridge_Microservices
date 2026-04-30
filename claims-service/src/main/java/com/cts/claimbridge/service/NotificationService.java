package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.Notification;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {
    @Autowired
    private ClaimRepository claimRepository;
    private final NotificationRepository notificationRepo;
    public void sendNotification(Long userId,Long claimId,String message,String category){
        Notification notification = new Notification();
        notification.setUserId(userId);
        Claim claim=claimRepository.findById(claimId).orElseThrow(()->new RuntimeException("No claim found"));
        notification.setClaim(claim);
        notification.setMessage(message);
        notification.setCategory(category);
        notification.setStatus("UNREAD");
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepo.save(notification);
    }
    public Page<Notification> getUserNotifications(Long userId, int page , int size) {
        Pageable pageable = PageRequest.of(page,size);
        return notificationRepo.findByUserId(userId,pageable);
    }
    public Notification markAsRead(Long id) {
        Notification n = notificationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setStatus("READ");
        return notificationRepo.save(n);
    }

}
