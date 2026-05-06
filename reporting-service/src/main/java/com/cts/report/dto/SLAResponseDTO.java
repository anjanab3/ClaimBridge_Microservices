package com.cts.report.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SLAResponseDTO {

    private Long slaId;
    private String name;
    private Integer responseHours;
    private Integer resolutionHours;
    private String monitoredEntity;
    private Boolean active;
}
