package com.cts.report.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
public class KPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long kpiId;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String definition;

    private BigDecimal target;
    private BigDecimal currentValue;
    private String reportingPeriod;   // DAILY, WEEKLY, MONTHLY, QUARTERLY
}
