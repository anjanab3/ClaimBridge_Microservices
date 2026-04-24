package com.cts.claimbridge.entity;

import java.math.BigDecimal;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class KPI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long KpiId;
    
    private String name;
    @Column(columnDefinition = "TEXT")
    private String definition;
    
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String reportingPeriod;     // DAILY, WEEKLY, MONTHLY, QUARTERLY

}