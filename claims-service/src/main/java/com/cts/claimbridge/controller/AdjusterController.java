package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.ClaimFullResponseDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.service.AdjusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasAnyAuthority('CLAIMS_ADJUSTER','ADMIN')")
@RequestMapping("/api/adjuster")
public class AdjusterController {

    @Autowired
    private AdjusterService adjusterService;

    @GetMapping("/claims/{adjusterId}")
    public ResponseEntity<?> getAssignedClaims(
            @PathVariable String adjusterId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
            Page<ClaimFullResponseDTO> result = adjusterService.getAssignedClaims(adjusterId, page, size);
            if(result.isEmpty()){
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Claims Assigned"));
            }
            return ResponseEntity.ok().body(result);

    }

    @GetMapping("/claims/{adjusterId}/{claimId}")
    public ResponseEntity<?> getAssignedClaimById(
            @PathVariable String adjusterId,
            @PathVariable Long claimId) {
            ClaimFullResponseDTO result = adjusterService.getAssignedClaimsById(adjusterId, claimId);
            if(result==null)
            {
                return ResponseEntity.ok().body(new ResponseDTO("No Claim Found"));
            }
            return ResponseEntity.ok().body(result);
    }
}