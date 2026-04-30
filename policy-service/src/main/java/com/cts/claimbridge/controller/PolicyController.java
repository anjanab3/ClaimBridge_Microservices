package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.service.PolicyService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
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

    @GetMapping("/search")
public ResponseEntity<?> getByNumber(@RequestParam String number) {
    Optional<Policy> policy = policyService.findByNumber(number);
    if (policy.isEmpty()) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ResponseDTO("Policy not found"));
    }
    return ResponseEntity.ok(policy.get());
}

    //  Get policy by ID
    //@PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping("/{policyId}")
    public Optional<Policy> getById(@PathVariable("policyId") Long policyId) {
        return policyService.findById(policyId);
    }

    // Get all policies
    //@PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Policy> policies = policyService.findAll(page , size);
        if(policies.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No policies found"));
        }

        return ResponseEntity.ok().body(policies);
    }

    //  Create a new policy
    //@PreAuthorize("hasAnyAuthority('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody List<Policy> policies) {
        try{
            return ResponseEntity.ok(policyService.savePolicies(policies));
        }
        catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    // Returns just the policyIds for a given holder — used by claims-service fraud scoring
    @GetMapping("/by-holder/{holderId}/ids")
    public List<Long> getPolicyIdsByHolderId(@PathVariable Long holderId) {
        return policyService.findPolicyIdsByHolderId(holderId);
    }
}
