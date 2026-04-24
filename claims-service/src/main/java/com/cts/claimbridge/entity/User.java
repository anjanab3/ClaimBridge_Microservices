package com.cts.claimbridge.entity;

import com.cts.claimbridge.util.Role;
import com.cts.claimbridge.util.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    private String username;
    @Email(message = "Invalid email format")
    @Column(unique = true)
    private String email;
    private String phone;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Column(name = "role_code", unique = true, nullable = false)
    private String roleCode;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "holder_id", nullable = true)
    private PolicyHolder policyHolder;
}
