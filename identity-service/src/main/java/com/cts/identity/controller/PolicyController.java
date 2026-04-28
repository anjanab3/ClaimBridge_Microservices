package com.cts.identity.controller;

import com.cts.identity.dto.MessageDTO;
import com.cts.identity.dto.PolicyRequestDTO;
import com.cts.identity.dto.ResponseDTO;
import com.cts.identity.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/identity/policies")
@PreAuthorize("hasAnyAuthority('ADMIN','UNDERWRITER')")
public class PolicyController {

    @Autowired private PolicyService policyService;

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(policyService.findAll(page, size));
    }

    @GetMapping("/{policyId}")
    public ResponseEntity<?> getById(@PathVariable Long policyId) {
        try {
            return ResponseEntity.ok(policyService.findById(policyId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PolicyRequestDTO req) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageDTO(policyService.create(req), "Policy created successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(e.getMessage()));
        }
    }

    @PutMapping("/{policyId}")
    public ResponseEntity<?> update(@PathVariable Long policyId, @RequestBody PolicyRequestDTO req) {
        try {
            return ResponseEntity.ok(new MessageDTO(policyService.update(policyId, req), "Policy updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO(e.getMessage()));
        }
    }
}
