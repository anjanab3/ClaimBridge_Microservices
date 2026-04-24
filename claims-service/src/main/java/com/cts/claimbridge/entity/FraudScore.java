package com.cts.claimbridge.entity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class FraudScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scoreId;

    @OneToOne //A
    @JoinColumn(name = "claim_id") //A
    @JsonBackReference(value = "score") //A
    private Claim claim;
    
    private Double scoreValue;
    @Column(columnDefinition = "TEXT")
    private String factorsJSON;
    private LocalDateTime calculatedAt;

//    @OneToOne
//    @JoinColumn(name = "alert_id")
//    private FraudAlert fraudAlert;

    @OneToOne
    @JoinColumn(name = "alert_id")
    @JsonBackReference(value = "alert-score") // Must match the value in FraudAlert
    private FraudAlert fraudAlert;
}
