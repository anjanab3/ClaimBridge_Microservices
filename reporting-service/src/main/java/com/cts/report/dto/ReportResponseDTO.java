package com.cts.report.dto;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ReportResponseDTO {

    private Long reportId;
    private String scope;
    private Map<String, Object> parametersJSON;
    private Map<String, Object> metricsJSON;
    private LocalDateTime generatedAt;
    private String reportUri;
    private String generatedBy;
}
