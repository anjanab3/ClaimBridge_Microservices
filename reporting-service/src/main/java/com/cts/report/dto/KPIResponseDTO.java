package com.cts.report.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
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
