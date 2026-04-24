package com.cts.claimbridge.entity;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

import java.time.LocalDateTime;

@Entity
@Data
public class Evidence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long evidenceId;
    private String fileName;
    private String fileType;
    private String filePath;
    private Boolean verified=false;
    private LocalDateTime uploadedAt;

    @ManyToOne //A
    @JoinColumn(name = "claim_id") //A
    @JsonBackReference(value ="claim") //A
    private Claim claim;//A

    @ManyToOne //A
    @JoinColumn(name = "investigation_id")//A
    @JsonBackReference(value="evi") //A
    private Investigation investigation; //A
}
