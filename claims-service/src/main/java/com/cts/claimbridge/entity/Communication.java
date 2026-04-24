package com.cts.claimbridge.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Data
@Entity
public class Communication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commId;
    private Long fromUserId;
    private Long toUserId;
    private String channel;
    
    @Column(nullable = false, length = 3000)
    private String message;
    private LocalDateTime sentAt;
    private String direction;   // OUTBOUND OR INBOUND
    private boolean isRead = false;

    @ManyToOne
    @JoinColumn(name="claim_id")
    @JsonBackReference(value = "comm")
    private Claim claim;

}