package com.cts.report.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "Notification")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;

    private Long userId;
    private Long claimId;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String category;    // INTAKE, INVESTIGATION, PAYMENT, FRAUD

    private String status;      // UNREAD, READ

    private LocalDateTime createdAt;
}
