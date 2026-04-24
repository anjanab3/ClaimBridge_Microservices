package com.cts.claimbridge.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class KPIRequestDTO {

    private String name;
    private String definition;
    private BigDecimal targetValue;
    private BigDecimal currentValue;
    private String reportingPeriod; // e.g., "Q1-2024", "MONTHLY", "ANNUAL"

}
