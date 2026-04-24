package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.TriageDecisionRequestDTO;
import com.cts.claimbridge.dto.TriageDecisionResponseDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.FraudAlert;
import com.cts.claimbridge.entity.TriageDecision;
import com.cts.claimbridge.entity.TriageRule;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.FraudAlertRepository;
import com.cts.claimbridge.repository.TriageDecisionRepository;
import com.cts.claimbridge.repository.TriageRuleRepository;
import com.cts.claimbridge.repository.UserRepository;
import com.cts.claimbridge.util.ClaimStatus;
import com.cts.claimbridge.util.Priority;
import com.cts.claimbridge.util.TriageStatus;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TriageDecisionService {

    @Autowired
    private TriageDecisionRepository decisionRepository;

    public List<TriageDecisionResponseDTO> getTriageDecision(Long claimId) {
        List<TriageDecision> decisions = decisionRepository.findByClaim_ClaimId(claimId);
        
        if (decisions == null || decisions.isEmpty()) {
            throw new RuntimeException("No triage decision found");
        }

        return decisions.stream()
                .map(decision -> new TriageDecisionResponseDTO(
                        decision.getDecisionId(),
                        decision.getRuleId(),
                        decision.getPriority(),
                        decision.getAssignedTo(),
                        decision.getAssignedAt()
                ))
                .collect(Collectors.toList());
    }
}