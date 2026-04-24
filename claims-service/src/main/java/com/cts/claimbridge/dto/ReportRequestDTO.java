package com.cts.claimbridge.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Getter
@Setter
public class ReportRequestDTO {

    private String scope;   // Scope of the report: Operational / Compliance / Fraud

    // Dynamic key-value parameters for report generation
    private Map<String, Object> parametersJson;     // e.g., { "startDate": "2026-01-01", "endDate": "2026-03-31", "region": "North" }

}   