package com.cts.claimbridge.service;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.Communication;
import com.cts.claimbridge.entity.Notification;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.CommunicationRepository;
import com.cts.claimbridge.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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

    public Communication addCommunication(Long claimId, Communication comm) {
        Optional<Claim> claim = claimRepository.findById(claimId);
        comm.setClaim(claim.get());
        if (comm.getSentAt() == null) {
            comm.setSentAt(LocalDateTime.now());
        }
        // Logic for direction
        if (comm.getDirection() == null) {
            comm.setDirection(comm.getToUserId() == null ? "INTERNAL" : "OUTBOUND");
        }
        return communicationRepository.save(comm);
    }

    public Page<Communication> getCommunicationsByClaim(Long claimId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return communicationRepository.findByClaim_ClaimIdOrderBySentAtDesc(claimId, pageable);
    }

    public List<Notification> getNotificationsByClaim(Long claimId) {
        return notificationRepository.findByClaim_ClaimIdOrderByCreatedAtDesc(claimId);
    }

    public Page<Communication> getCommunicationsByUserId(long userId, int page , int size) {
          Pageable pageable = PageRequest.of(page ,size);
          return communicationRepository.findByToUserId(userId,pageable);
    }
//    public Notification sendNotification(Long claimId, Notification notification) {
//        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new RuntimeException("Claim not found with id: " + claimId));
//        notification.setClaim(claim);
//        notification.setCreatedAt(LocalDateTime.now());
//
//        if (notification.getStatus() == null) {
//            notification.setStatus("SENT");
//        }
//        return notificationRepository.save(notification);
//    }
//
//    public Page<Notification> getNotificationsByClaim(Long claimId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        return notificationRepository.findByClaim_ClaimIdOrderByCreatedAtDesc(claimId, pageable);
//    }



}