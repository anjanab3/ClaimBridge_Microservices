package com.cts.payment.entity;

import com.cts.payment.util.SettlementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "settlement")
@AllArgsConstructor
@NoArgsConstructor
public class Settlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    @Column(nullable = false)
    private Long claimId;

    private Double recommendedAmount;
    private LocalDateTime recommendedAt;
    private String recommendedBy;

    @Enumerated(EnumType.STRING)
    private SettlementStatus status;
}
