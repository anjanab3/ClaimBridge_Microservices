package com.cts.identity.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "policyholders")
@Data
public class PolicyHolder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long holderId;

    @Column(nullable = false)
    private String name;

    private String contactInfo;
    private String businessType;
    private String taxID;

    @OneToMany(mappedBy = "holder", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "holder-policies")
    private List<Policy> policies;

    @OneToMany(mappedBy = "policyHolder", cascade = CascadeType.ALL)
    @JsonManagedReference(value = "holder-users")
    private List<User> userList;
}
