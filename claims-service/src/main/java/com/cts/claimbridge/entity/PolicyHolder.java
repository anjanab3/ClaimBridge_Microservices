package com.cts.claimbridge.entity;

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

    @OneToMany(mappedBy = "holder",cascade = CascadeType.ALL)//A
    private List<Policy> policy;//A

    @OneToMany(mappedBy="policyHolder",cascade = CascadeType.ALL)
    private List<User> userList;

}
