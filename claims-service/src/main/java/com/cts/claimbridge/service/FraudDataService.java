package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.FraudAlertDTO;
import com.cts.claimbridge.dto.FraudDataResponseDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.FraudAlert;
import com.cts.claimbridge.entity.FraudScore;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.FraudAlertRepository;
import com.cts.claimbridge.repository.FraudScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FraudDataService {
    @Autowired
    private FraudScoreRepository scoreRepository;
    @Autowired
    private FraudAlertRepository alertRepository;
    @Autowired
    private ClaimRepository claimRepository;

    //Returns all claims that have been scored by the fraud engine.
    public List<FraudDataResponseDTO> getAllFlaggedClaims() {
        List<FraudScore> scores = scoreRepository.findAll();
        if (scores.isEmpty()) return null;

        return scores.stream().map(score -> {
            List<FraudAlert> alerts = alertRepository.findByClaim_ClaimId(score.getClaim().getClaimId());

            List<FraudAlertDTO> alertDTOs = alerts.stream().map(a -> {
                FraudAlertDTO ad = new FraudAlertDTO();
                ad.setAlertId(a.getAlertId());
                ad.setScoreId(a.getFraudScore() != null ? a.getFraudScore().getScoreId() : null);
                ad.setReason(a.getReason());
                ad.setEscalatedTo(a.getEscalatedTo());
                ad.setEscalatedAt(a.getEscalatedAt());
                ad.setStatus(a.getStatus());
                return ad;
            }).collect(Collectors.toList());

            FraudDataResponseDTO dto = new FraudDataResponseDTO();
            dto.setScoreId(score.getScoreId());
            dto.setClaimId(score.getClaim().getClaimId());       // direct field — no lazy-load risk
            dto.setScoreValue(score.getScoreValue());
            dto.setFactorsJSON(score.getFactorsJSON());
            dto.setCalculatedAt(score.getCalculatedAt());
            dto.setAlerts(alertDTOs.isEmpty() ? null : alertDTOs);
            return dto;
        }).collect(Collectors.toList());
    }

    public FraudDataResponseDTO getFraudData(Long claimId) { //A
        // 1. Fetch fraud score
        FraudScore score = scoreRepository.findByClaim_ClaimId(claimId)
                .orElseThrow(() -> new RuntimeException("Fraud score not found for claim " + claimId));
        // 2. Fetch all alerts
        List<FraudAlert> alerts = alertRepository.findByClaim_ClaimId(claimId);
        // 3. Prepare response
        Optional<Claim> claim=claimRepository.findById(claimId);
        FraudDataResponseDTO dto = new FraudDataResponseDTO();
        dto.setScoreId(score.getScoreId());
        dto.setClaimId(score.getClaim().getClaimId());
        dto.setScoreValue(score.getScoreValue());
        dto.setFactorsJSON(score.getFactorsJSON());
        dto.setCalculatedAt(score.getCalculatedAt());
        // 4. Map alerts
        List<FraudAlertDTO> alertDTOs = alerts.stream().map(a -> {
            FraudAlertDTO ad = new FraudAlertDTO();
            ad.setAlertId(a.getAlertId());
          //  ad.setScoreId(a.getFraudScore() != null ? a.getFraudScore().getScoreId() : null);
            ad.setReason(a.getReason());
            ad.setEscalatedTo(a.getEscalatedTo());
            ad.setEscalatedAt(a.getEscalatedAt());
            ad.setStatus(a.getStatus());
            return ad;
        }).collect(Collectors.toList());
        dto.setAlerts(alertDTOs);
        return dto;
    }

//    Returns all claims that have been scored by the fraud engine.
//    public List<FraudDataResponseDTO> getAllFlaggedClaims() {
//        List<FraudScore> scores = scoreRepo.findAll();
//        if (scores.isEmpty()) return null;
//
//        return scores.stream().map(score -> {
//            List<FraudAlert> alerts = alertRepo.findByClaim_ClaimId(score.getClaimId());
//
//            List<FraudAlertDTO> alertDTOs = alerts.stream().map(a -> {
//                FraudAlertDTO ad = new FraudAlertDTO();
//                ad.setAlertId(a.getAlertId());
//                ad.setScoreId(a.getFraudScore() != null ? a.getFraudScore().getScoreId() : null);
//                ad.setReason(a.getReason());
//                ad.setEscalatedTo(a.getEscalatedTo());
//                ad.setEscalatedAt(a.getEscalatedAt());
//                ad.setStatus(a.getStatus());
//                return ad;
//            }).collect(Collectors.toList());
//
//            FraudDataResponseDTO dto = new FraudDataResponseDTO();
//            dto.setScoreId(score.getScoreId());
//            dto.setClaimId(score.getClaimId());       // direct field — no lazy-load risk
//            dto.setScoreValue(score.getScoreValue());
//            dto.setFactorsJSON(score.getFactorsJSON());
//            dto.setCalculatedAt(score.getCalculatedAt());
//            dto.setAlerts(alertDTOs.isEmpty() ? null : alertDTOs);
//            return dto;
//        }).collect(Collectors.toList());
//    }
}

