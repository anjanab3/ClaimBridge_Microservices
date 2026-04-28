package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.ClaimFullResponseDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.service.AdjusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
//@PreAuthorize("hasAuthority('CLAIMS_ADJUSTER')")
@RequestMapping("/api/adjuster")
public class AdjusterController {

    @Autowired
    private AdjusterService adjusterService;

    @GetMapping("/claims/{adjusterId}")
    public ResponseEntity<?> getAssignedClaims(
            @PathVariable String adjusterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("Fetching assigned claims for adjusterId: {}", adjusterId);
        Page<ClaimFullResponseDTO> result = adjusterService.getAssignedClaims(adjusterId, page, size);
        if(result == null) {
            log.warn("Adjuster not found with adjusterId: {}", adjusterId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("Adjuster Not Found"));
        }
        if(result.isEmpty()){
            log.warn("No claims assigned to adjusterId: {}", adjusterId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Claims Assigned"));
        }
        log.info("Returning {} claims for adjusterId: {}", result.getTotalElements(), adjusterId);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/claims/{adjusterId}/{claimId}")
    public ResponseEntity<?> getAssignedClaimById(
            @PathVariable("adjusterId") String adjusterId,
            @PathVariable("claimId") Long claimId) {
        log.info("Request received to fetch claimId: {} for adjusterId: {}", claimId, adjusterId);
        ClaimFullResponseDTO result = adjusterService.getAssignedClaimsById(adjusterId, claimId);
        if(result == null) {
            log.warn("No claim found with claimId: {} for adjusterId: {}", claimId, adjusterId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Claim Found"));
        }
        log.info("Successfully retrieved claimId: {} for adjusterId: {}", claimId, adjusterId);
        return ResponseEntity.ok().body(result);
    }
}