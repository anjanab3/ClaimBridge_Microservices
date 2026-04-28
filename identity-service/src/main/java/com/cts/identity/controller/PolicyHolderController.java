package com.cts.identity.controller;

import com.cts.identity.dto.MessageDTO;
import com.cts.identity.dto.PolicyHolderRequestDTO;
import com.cts.identity.dto.ResponseDTO;
import com.cts.identity.service.PolicyHolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/identity/policyholders")
@PreAuthorize("hasAuthority('ADMIN')")
public class PolicyHolderController {

    @Autowired private PolicyHolderService policyHolderService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(policyHolderService.findAll(page, size));
    }

    @GetMapping("/{holderId}")
    public ResponseEntity<?> getById(@PathVariable Long holderId) {
        try {
            return ResponseEntity.ok(policyHolderService.findById(holderId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PolicyHolderRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageDTO(policyHolderService.create(req), "PolicyHolder created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    @PutMapping("/{holderId}")
    public ResponseEntity<?> update(@PathVariable Long holderId, @RequestBody PolicyHolderRequestDTO req) {
        try {
            return ResponseEntity.ok(new MessageDTO(policyHolderService.update(holderId, req), "PolicyHolder updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }
}
