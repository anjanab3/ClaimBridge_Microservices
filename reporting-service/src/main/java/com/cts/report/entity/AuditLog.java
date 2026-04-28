package com.cts.report.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "AuditLog")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    private Long userId;
    private String resource;        // Claim, FraudAlert, Policy, Investigation
    private Long resourceId;
    private String action;          // LOGIN, UPDATE, DELETE, VIEW, STATUS_CHANGE
    private String details;
    private LocalDateTime timestamp;
}
