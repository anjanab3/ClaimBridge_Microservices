package com.cts.claimbridge.entity;

import java.time.LocalDateTime;
import java.util.Map;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long auditId;
    
    private Long userId;
    private String action;
    private String resource;
    private LocalDateTime timestamp;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> details;

}