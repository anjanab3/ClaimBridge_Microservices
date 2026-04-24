package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.FraudDataResponseDTO;
import com.cts.claimbridge.service.FraudDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
public class FraudDataController {
    @Autowired
    private FraudDataService fraudService;

    @GetMapping("/{claimId}/fraud_data") //A
    public FraudDataResponseDTO getFraudData(@PathVariable Long claimId) {
        return fraudService.getFraudData(claimId);
    }
}
