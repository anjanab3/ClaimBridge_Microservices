package com.cts.identity.dto;

import com.cts.identity.util.PolicyStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PolicyResponseDTO {
    private Long policyId;
    private String policyNumber;
    private String insuredName;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String coverageJSON;
    private PolicyStatus status;
    private Long holderId;
}
