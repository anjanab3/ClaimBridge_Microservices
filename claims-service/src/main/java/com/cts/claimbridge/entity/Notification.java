package com.cts.claimbridge.entity;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class  Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationID;
    private Long   userId;      // policyholder (numeric Long)
    private String staffUserId; // adjuster / fraud analyst (e.g. "CA-0001")
    private String message;
    private String category;
    private String status;
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "claim_id")
    @JsonBackReference(value = "notification")
    private Claim claim;

}