package com.cts.claimbridge.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long reportId;
    private String scope;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> parametersJSON;
    
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metricsJSON;
    
    private LocalDateTime generatedAt;
    private String reportUri;

}