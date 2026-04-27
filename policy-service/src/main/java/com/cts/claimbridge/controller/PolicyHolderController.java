package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.entity.PolicyHolder;
import com.cts.claimbridge.service.PolicyHolderService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/policyholders")
@RequiredArgsConstructor
public class PolicyHolderController {

    @Autowired
    private  PolicyHolderService holderService;
    // @Autowired
    // private  ClaimService claimService;

    //create policyHolder
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<?> createHolder(@RequestBody List<PolicyHolder> holder){
        try {
            return ResponseEntity.ok().body(new MessageDTO(holderService.save(holder),"PolicyHolder added sucessfully!!!"));
        }
        catch(Exception e)
        {
            return ResponseEntity.badRequest().body(new ResponseDTO("Policy holder creation failed!!!"));
        }

    }

    //update holder details
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{holderId}")
    public ResponseEntity<?> updateHolder(@PathVariable Long holderId,@RequestBody PolicyHolder holder){
        try {
            return ResponseEntity.ok().body(new MessageDTO(holderService.update(holderId,holder),"Holder details updated successfully !!!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO("No Holder Id found"));
        }
    }

    // Get all policyholders
    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PolicyHolder> policyholders =  holderService.findAll(page , size);
        if(policyholders.isEmpty())
        {
            return ResponseEntity.ok().body(new ResponseDTO("No Holder Found"));
        }

        return ResponseEntity.ok().body(policyholders);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_ADJUSTER')")
    @GetMapping("/{holderId}")
    public ResponseEntity<?> getPolicyHolderById(@PathVariable long holderId) {
        Optional<PolicyHolder> policyholder =  holderService.findById(holderId);
        if(policyholder.get() == null)
        {
            return ResponseEntity.ok().body(new ResponseDTO("No Holder Found"));
        }

        return ResponseEntity.ok().body(policyholder.get());
    }

    // Get all policies for a holder
    @PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_ADJUSTER')")
    @GetMapping("/{holderId}/policies")
    public List<Policy> getPoliciesForHolder(@PathVariable Long holderId) {
        return holderService.findPoliciesForHolder(holderId);
    }

}
