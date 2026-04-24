package com.cts.claimbridge.controller;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.entity.Settlement;
import com.cts.claimbridge.service.SettlementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@PreAuthorize("hasAuthority('PAYOUT_OFFICER')")
@RequestMapping("/api/settlement")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    //Get settlement
    @GetMapping("/{claimId}/settlement")
    public ResponseEntity<List<Settlement>> getSettlement(@PathVariable Long claimId) {
        List<Settlement> settlements = settlementService.getSettlementsByClaim(claimId);
        return ResponseEntity.ok(settlements);
    }

    //Approve or reject settlement
    @PostMapping("/{settlementId}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long settlementId, @RequestBody Map<String, String> statusUpdate) {
        String newStatus = statusUpdate.get("status");
       Optional<Settlement> settlement = settlementService.updateStatus(settlementId, newStatus);
       if(settlement.isEmpty())
       {
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseDTO("Settlement Not Found"));
       }
       return ResponseEntity.ok().body(new ResponseDTO("Settlement Status Changed!!!"));
    }
}