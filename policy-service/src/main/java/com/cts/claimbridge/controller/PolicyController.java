package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.service.PolicyService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    @Autowired
    private  PolicyService policyService;

    //  Get policy by ID
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/{policyId}")
    public Optional<Policy> getById(@PathVariable Long policyId) {
        return policyService.findById(policyId);
    }

    // Get all policies
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Policy> policies = policyService.findAll(page , size);
        if(policies.isEmpty())
        {
            return ResponseEntity.ok().body(new ResponseDTO("No policies found"));
        }

        return ResponseEntity.ok().body(policies);
    }

    //  Create a new policy
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody List<Policy> policies) {
        try{
            return ResponseEntity.ok(policyService.save(policies));
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

