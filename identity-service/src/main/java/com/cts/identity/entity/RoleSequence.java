package com.cts.identity.entity;

import com.cts.identity.util.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_sequence")
@Data
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
