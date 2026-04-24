package com.cts.claimbridge.service;


import com.cts.claimbridge.dto.EvidenceDTO;
import com.cts.claimbridge.dto.VerifyevidenceRequestDTO;
import com.cts.claimbridge.entity.Claim;
import com.cts.claimbridge.entity.Evidence;
import com.cts.claimbridge.entity.PolicyHolder;
import com.cts.claimbridge.repository.ClaimRepository;
import com.cts.claimbridge.repository.EvidenceRepository;
import com.cts.claimbridge.repository.PolicyHolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EvidenceService {

    @Autowired
    private EvidenceRepository evidenceRepository;
    @Autowired
    private ClaimRepository claimRepository;
    @Autowired
    private PolicyHolderRepository policyHolderRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;


    public boolean uploadfile(long holderId , long claimId , MultipartFile file) throws Exception { //A

        Claim claim = claimRepository.findById(claimId).orElseThrow(() -> new RuntimeException("Claim Not Found for ID : " + claimId));
        PolicyHolder holder = policyHolderRepository.findById(holderId).orElseThrow(()-> new RuntimeException("Holder Not Found for ID " + holderId));
        Path uploadPath = Paths.get(uploadDir,String.valueOf(holderId),String.valueOf(claimId));
        if(!Files.exists(uploadPath))
        {
            Files.createDirectories(uploadPath);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetPath = uploadPath.resolve(fileName);

        Files.copy(file.getInputStream() , targetPath , StandardCopyOption.REPLACE_EXISTING);

        Evidence evidence = new Evidence();
        evidence.setFileName(fileName);
        evidence.setFileType(file.getContentType());
        evidence.setFilePath(targetPath.toString());
        evidence.setClaim(claim);
        evidence.setUploadedAt(LocalDateTime.now());
        evidenceRepository.save(evidence);
        System.out.println(evidence);

        return true;
    }


    public List<EvidenceDTO> getEvidenceByClaimId(Long claimId){ //A
        List<Evidence> evidenceList=evidenceRepository.findByClaim_ClaimId(claimId);
        return evidenceList.stream()
                .map(e->{
                    EvidenceDTO dto=new EvidenceDTO();
                    dto.setEvidenceId(e.getEvidenceId());
                    dto.setFileName(e.getFileName());
                    dto.setFileType(e.getFileType());
                    dto.setFilePath(e.getFilePath());
                    dto.setIsVerified(e.getVerified());
                    dto.setUploadedAt(e.getUploadedAt());
                    return dto;
                }).collect(Collectors.toList());
    }

    public EvidenceDTO verifyEvidence(Long claimId, Long evidenceId,@RequestBody VerifyevidenceRequestDTO dto){ //A
            Evidence evidence = evidenceRepository.findById(evidenceId)
                    .orElseThrow(() -> new RuntimeException("Evidence not found"));
//            if (!evidence.getClaim().getClaimId().equals(claimId)) {
//                throw new RuntimeException("Evidence does not belong to this claim");
//            }
            evidence.setVerified(dto.getIsVerified());
            return mapToDTO(evidenceRepository.save(evidence));
        }

        private EvidenceDTO mapToDTO(Evidence e) { //A
            EvidenceDTO dto = new EvidenceDTO();
            dto.setEvidenceId(e.getEvidenceId());
            dto.setFileName(e.getFileName());
            dto.setFileType(e.getFileType());
            dto.setFilePath(e.getFilePath());
            dto.setIsVerified(e.getVerified());
            dto.setUploadedAt(e.getUploadedAt());
            return dto;
        }

//    public byte[] getEvidences(long holderId, long claimId , long evidenceId) throws Exception {
//        Evidence evidence = evidenceRepository.findByHolderAndClaimAndEvidenceId(holderId, claimId, evidenceId).orElseThrow(() -> new RuntimeException("Evidence Record Not Found :("));
//        Path path = Paths.get(evidence.getFilePath());
//        if(!Files.exists(path))
//        {
//            throw new FileNotFoundException("Evidence File not Found");
//        }
//        System.out.println(path);
//        return Files.readAllBytes(path);
//    }
//    public Optional<Evidence> getEvidenceMetadata(long holderId, long claimId, long evidenceId) {
//        return evidenceRepository.findByHolderAndClaimAndEvidenceId(holderId , claimId , evidenceId);
//    }

    }

