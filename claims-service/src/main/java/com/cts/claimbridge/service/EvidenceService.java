package com.cts.claimbridge.service;

import com.cts.claimbridge.client.PolicyServiceClient;
import com.cts.claimbridge.dto.EvidenceDTO;
import com.cts.claimbridge.dto.PolicyHolderDTO;
import com.cts.claimbridge.dto.VerifyevidenceRequestDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.Evidence;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.EvidenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EvidenceService {

    @Autowired
    private EvidenceRepository evidenceRepository;
    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private PolicyServiceClient policyServiceClient;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public boolean uploadfile(long holderId, long claimId, MultipartFile file) throws Exception {

        // Validate claim exists in claims-service DB
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found for ID: " + claimId));

        // Validate holder exists via policy-service Feign call
        PolicyHolderDTO holder = fetchHolder(holderId);
        if (holder == null) {
            throw new RuntimeException("Holder not found for ID: " + holderId);
        }

        Path uploadPath = Paths.get(uploadDir, String.valueOf(holderId), String.valueOf(claimId));
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetPath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Evidence evidence = new Evidence();
        evidence.setFileName(fileName);
        evidence.setFileType(file.getContentType());
        evidence.setFilePath(targetPath.toString());
        evidence.setClaim(claim);
        evidence.setUploadedAt(LocalDateTime.now());
        evidenceRepository.save(evidence);

        return true;
    }

    public List<EvidenceDTO> getEvidenceByClaimId(Long claimId) {
        List<Evidence> evidenceList = evidenceRepository.findByClaim_ClaimId(claimId);
        return evidenceList.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public EvidenceDTO verifyEvidence(Long claimId, Long evidenceId, VerifyevidenceRequestDTO dto) {
        Evidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException("Evidence not found"));
        evidence.setVerified(dto.getIsVerified());
        return mapToDTO(evidenceRepository.save(evidence));
    }

    private PolicyHolderDTO fetchHolder(long holderId) {
        try {
            return policyServiceClient.getPolicyHolderById(holderId);
        } catch (Exception e) {
            return null;
        }
    }

    private EvidenceDTO mapToDTO(Evidence e) {
        EvidenceDTO dto = new EvidenceDTO();
        dto.setEvidenceId(e.getEvidenceId());
        dto.setFileName(e.getFileName());
        dto.setFileType(e.getFileType());
        dto.setFilePath(e.getFilePath());
        dto.setIsVerified(e.getVerified());
        dto.setUploadedAt(e.getUploadedAt());
        return dto;
    }
}