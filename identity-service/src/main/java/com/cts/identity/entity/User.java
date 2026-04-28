package com.cts.identity.entity;

import com.cts.identity.util.Role;
import com.cts.identity.util.UserStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    private String userId;           // role-code based ID e.g. ADM-0001

    @Column(unique = true, nullable = false)
    private String username;

    @Email(message = "Invalid email format")
    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "holder_id", nullable = true)
    @JsonBackReference
    private PolicyHolder policyHolder;
}
