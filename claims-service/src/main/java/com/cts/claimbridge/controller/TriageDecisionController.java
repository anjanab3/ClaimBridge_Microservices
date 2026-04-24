package com.cts.claimbridge.controller;


import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.service.TriageDecisionService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/triage/decisions")
@PreAuthorize("hasAnyAuthority('ADMIN','CLAIMS_INTAKE_AGENT','CLAIMS_ADJUSTER')")
public class TriageDecisionController {
    @Autowired
   private TriageDecisionService service;
   @GetMapping("/{claimId}/triage_decision") //A
   public List<TriageDecisionResponseDTO> getTriageDecision(@PathVariable Long claimId) {
       return service.getTriageDecision(claimId);
}

}
