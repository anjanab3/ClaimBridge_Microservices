package com.cts.claimbridge.entity;

import com.cts.claimbridge.util.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class RoleSequence {
    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @Column(name = "last_sequence", nullable = false)
    private int lastSequence;

}
