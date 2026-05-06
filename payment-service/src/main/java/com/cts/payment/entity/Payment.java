package com.cts.payment.entity;

import com.cts.payment.util.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "Payment")
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    // Plain column — no cross-DB JPA join to claims-service
    @Column(nullable = false)
    private Long settlementId;

    // Plain column — no cross-DB JPA join to claims-service
    @Column(nullable = false)
    private Long claimId;

    private String payee;
    private Double amount;
    private String method;
    private LocalDate scheduledDate;
    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String reference;

    //     @ManyToOne
//     @JoinColumn(name = "settlementId", nullable = false)
//     @JsonIgnoreProperties("payments")
//     private Settlement settlement;
}