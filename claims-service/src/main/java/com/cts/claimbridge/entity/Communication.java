package com.cts.claimbridge.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Communication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commId;

    private String fromUserId;  // ← change Long to String
    private String toUserId;    // ← change Long to String

    private String channel;

    @Column(nullable = false, length = 3000)
    private String message;
    private LocalDateTime sentAt;
    private String direction;
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    @JsonBackReference(value = "comm")
    private Claim claim;
}