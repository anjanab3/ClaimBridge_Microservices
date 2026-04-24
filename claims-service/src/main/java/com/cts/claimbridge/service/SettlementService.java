package com.cts.claimbridge.service;
import com.cts.claimbridge.entity.Settlement;
import com.cts.claimbridge.repository.SettlementRepository;
import com.cts.claimbridge.util.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class SettlementService {

    @Autowired
    private SettlementRepository settlementRepository;

    public List<Settlement> getSettlementsByClaim(Long claimId) {
        return settlementRepository.findByClaim_ClaimId(claimId);
    }

    public Optional<Settlement> updateStatus(Long settlementId, String newStatus) {
        return settlementRepository.findById(settlementId)
                .map(settlement -> {
                    settlement.setStatus(Status.valueOf(newStatus));
                    return settlementRepository.save(settlement);
                });
    }
}