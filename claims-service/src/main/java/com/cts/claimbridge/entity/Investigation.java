package com.cts.claimbridge.entity;

import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.util.InvestigationStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Investigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long investigationId;
    private Long assignedAdjusterId;
    @Enumerated(EnumType.STRING)
    private InvestigationStatus status;     // OPEN, IN_PROGRESS, CLOSED
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    @OneToOne //A
    @JoinColumn(name = "claimId") //A
    @JsonBackReference(value = "inv") //A
    private Claim claim; //A

    @OneToMany(mappedBy = "investigation", cascade = CascadeType.ALL) //A
    @JsonBackReference(value = "invNote") //A
    private List<InvestigationNote> investigationNote; //A

    @OneToMany(mappedBy = "investigation", cascade = CascadeType.ALL)
    @JsonBackReference(value = "evi")
    private  List<Evidence> evidenceList;
}