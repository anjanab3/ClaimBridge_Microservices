package com.cts.payment.entity;

import com.cts.payment.util.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    // Plain column — no cross-DB JPA join to claims-service
    @Column(nullable = false)
    private Long claimId;

    private Double recommendedAmount;
    private String recommendedBy;
    private LocalDateTime recommendedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "settlement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;
}
