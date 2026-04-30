package com.cts.claimbridge.entity;

import com.cts.claimbridge.util.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Settlement")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;
    private Double recommendedAmount;
    private String recommendedBy;
    private LocalDateTime recommendedAt;
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne//A
    @JoinColumn(name = "claimId")//A
    @JsonBackReference(value = "set")//A
    private Claim claim;//A

    
}
