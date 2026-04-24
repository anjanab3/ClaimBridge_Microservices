package com.cts.claimbridge.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
public class ReportResponseDTO {
    
    private Long reportId;
    private String scope;
    private Map<String, Object> parametersJson;
    private Map<String, Object> metricsJson;
    private LocalDateTime generatedAt;
    private String reportUri;
    private String message;

}
