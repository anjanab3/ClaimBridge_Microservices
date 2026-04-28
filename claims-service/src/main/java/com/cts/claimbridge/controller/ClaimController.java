package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.ClaimStatusDTO;
import com.cts.claimbridge.dto.MessageDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.dto.ValidationRequestDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.service.ClaimService;
import com.cts.claimbridge.util.ClaimStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    @Autowired
    private  ClaimService claimService;

    // Submit a claim for a holder
    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{holderId}/{policyId}")
    public ResponseEntity<?> submitClaim(@PathVariable long holderId, @PathVariable long policyId , @RequestBody Claim claim) {
      try {
          return ResponseEntity.ok().body(new MessageDTO(claimService.save(claim, policyId, holderId),"Claim Created Successfully !!!"));
      }
      catch(Exception e)
      {
          return ResponseEntity.badRequest().body(new ResponseDTO("Claim submission failed!!!"));
      }
    }

   // View all
    @PreAuthorize("hasAuthority('CLAIMS_INTAKE_AGENT')")
    @GetMapping()
    public ResponseEntity<?> getAllClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Claim> allClaims = claimService.findAllClaims(page, size);
         if(allClaims.isEmpty())
         {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No Claims Found");
         }
         return ResponseEntity.ok().body(allClaims);
    }

    // View all claims for a holder
    @PreAuthorize("hasAnyAuthority('USER','CLAIMS_INTAKE_AGENT')")
    @GetMapping("/{holderId}")
    public List<Claim> getAllClaims(@PathVariable Long holderId) {
        return claimService.findByHolder(holderId);
    }

    //reviews incoming claim
    @PreAuthorize("hasAnyAuthority('CLAIMS_INTAKE_AGENT')")
    @GetMapping("/incoming")
    public ResponseEntity<?> getIncomingClaims(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Claim>  claims = claimService.getIncomingClaims(page, size);
        if(claims.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("No Incoming Claims Found "));
        }

        return ResponseEntity.ok().body(claims);
    }

    // get a specific claim of a holder
    @PreAuthorize("hasAnyAuthority('USER','CLAIMS_INTAKE_AGENT')")
    @GetMapping("/{holderId}/{claimId}")
    public Optional<Claim> getClaim(@PathVariable Long claimId) {
        return claimService.findById(claimId);
    }

    //validates policy coverage
    //changing the status of the claim
    @PreAuthorize("hasAnyAuthority('CLAIMS_INTAKE_AGENT')")
    @PutMapping("/{claimId}/validation")
    public ResponseEntity<?> validateCoverage(@PathVariable Long claimId , @RequestBody ValidationRequestDTO request) {
         Claim updatedclaim = claimService.validateClaim(claimId , ClaimStatus.valueOf(request.getStatus()));
         return ResponseEntity.ok().body(updatedclaim);
    }

    // Get the status of a specific claim
    @PreAuthorize("hasAnyAuthority('USER','CLAIMS_INTAKE_AGENT')")
    @GetMapping("/{claimId}/status")
    public ResponseEntity<ClaimStatusDTO> getClaimStatus(@PathVariable Long claimId) {
        return ResponseEntity.ok(claimService.getClaimStatus(claimId));
    }
}
