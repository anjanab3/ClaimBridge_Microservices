package com.cts.report.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "SLA")
public class SLA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slaId;

    @Column(nullable = false)
    private String name;

    private Integer responseHours;      // Max hours before first action is taken

    private Integer resolutionHours;    // Max total hours to reach terminal state

    private String monitoredEntity;     // CLAIM, INVESTIGATION, FRAUD_ALERT, SETTLEMENT

    private Boolean active;
}
