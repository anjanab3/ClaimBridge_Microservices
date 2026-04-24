package com.cts.claimbridge.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import java.util.Map;

@Builder
@Getter
@Setter
public class AuditLogResponseDTO {
    
    private Long auditId;
    private Long userId;
    // e.g., "CREATE", "UPDATE", "DELETE"
    private String action;
    // e.g., "CLAIM", "FRAUD ALERT", "POLICY"
    private String resource;
    // JSON string with additional info about the action
    private Map<String, Object> details;
    private LocalDateTime timestamp;

}
