package com.cts.report.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class KPIResponseDTO {

    private Long kpiId;
    private String name;
    private String definition;
    private BigDecimal target;
    private BigDecimal currentValue;
    private String reportingPeriod;
    private String trend;           // UP, DOWN, STABLE
}
