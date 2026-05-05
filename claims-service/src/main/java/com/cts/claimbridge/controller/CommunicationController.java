package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.Communication;
import com.cts.claimbridge.entity.Notification;
import com.cts.claimbridge.service.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class CommunicationController {

    @Autowired
    private CommunicationService communicationService;

    @PostMapping("/{claimID}/communications")
    public ResponseEntity<?> createComm(
            @PathVariable Long claimID,
            @RequestBody Communication comm) {
        try {
            Communication savedComm = communicationService.addCommunication(claimID, comm);
            return ResponseEntity.ok().body(new MessageDTO(savedComm,"Message send Successfully !!!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO("No claim Id found"));
        }
    }


    @GetMapping("/{claimID}/communications")
    public ResponseEntity<?> listComms(
            @PathVariable Long claimID,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Communication> communications = communicationService.getCommunicationsByClaim(claimID, page, size);
            return ResponseEntity.ok(communications);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{userId}/communication")
    public ResponseEntity<?> getCommunicationByUserId(
        @PathVariable("userId") String userId,  // String not long
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "10") int size) {
    Page<Communication> communication = communicationService
            .getCommunicationsByUserId(userId, page, size);
    if (communication.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO("No communication Found"));
    }
        return ResponseEntity.ok().body(communication);
    }

}