package com.cts.report.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class ReportRequestDTO {

    private String scope;                       // OPERATIONAL, COMPLIANCE, FRAUD
    private Map<String, Object> parametersJSON; // e.g., { "startDate": "2026-01-01", "region": "North" }
}
