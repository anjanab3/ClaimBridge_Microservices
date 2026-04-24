package com.cts.claimbridge.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class InvestigationNote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteId;

    @Column(nullable = false)
    private String authorId;
   
    private String noteText;
    private LocalDateTime createdAt;
    
    @ManyToOne //A
    @JoinColumn(name = "investigationId") //A
    @JsonBackReference(value = "invNote") //A
    private Investigation investigation; //A

    

}
