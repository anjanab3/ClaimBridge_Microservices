package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.Communication;
import com.cts.claimbridge.service.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/claims")
public class CommunicationController {

    @Autowired
    private CommunicationService communicationService;

    // ── Send a message ────────────────────────────────────────────────────────
    @PostMapping("/{claimID}/communications")
    public ResponseEntity<?> createComm(
            @PathVariable Long claimID,
            @RequestBody Communication comm) {
        try {
            Communication saved = communicationService.addCommunication(claimID, comm);
            return ResponseEntity.ok(new MessageDTO(saved, "Message sent successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    // ── Get all messages for a claim (paginated, newest first) ───────────────
    @GetMapping("/{claimID}/communications")
    public ResponseEntity<?> listComms(
            @PathVariable Long claimID,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        try {
            Page<Communication> comms = communicationService.getCommunicationsByClaim(claimID, page, size);
            return ResponseEntity.ok(comms);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ── Get all messages received by a user (inbox) ───────────────────────────
    @GetMapping("/{userId}/communication")
    public ResponseEntity<?> getCommunicationByUserId(
            @PathVariable("userId") String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Communication> comms = communicationService.getCommunicationsByUserId(userId, page, size);
        if (comms.isEmpty()) {
            return ResponseEntity.ok(comms); // return empty page instead of 404
        }
        return ResponseEntity.ok(comms);
    }

    // ── Count unread messages for a user ──────────────────────────────────────
    @GetMapping("/{userId}/communications/unread-count")
    public ResponseEntity<?> getUnreadCount(@PathVariable String userId) {
        long count = communicationService.countUnreadByUserId(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    // ── Mark a single message as read ─────────────────────────────────────────
    @PutMapping("/communications/{commId}/read")
    public ResponseEntity<?> markCommRead(@PathVariable Long commId) {
        try {
            Communication updated = communicationService.markAsRead(commId);
            return ResponseEntity.ok(new MessageDTO(updated, "Message marked as read"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    // ── Mark all messages for a claim sent to a specific user as read ─────────
    @PutMapping("/{claimId}/communications/mark-read")
    public ResponseEntity<?> markAllRead(
            @PathVariable Long claimId,
            @RequestParam String userId) {
        communicationService.markAllReadForClaim(claimId, userId);
        return ResponseEntity.ok(new ResponseDTO("All messages marked as read"));
    }
}
