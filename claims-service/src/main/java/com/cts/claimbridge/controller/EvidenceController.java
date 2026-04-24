package com.cts.claimbridge.controller;
import com.cts.claimbridge.dto.EvidenceDTO;
import com.cts.claimbridge.dto.ResponseDTO;
import com.cts.claimbridge.dto.VerifyevidenceRequestDTO;
import com.cts.claimbridge.service.EvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/claims")
public class EvidenceController {
    @Autowired
    private EvidenceService evidenceService;

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{holderId}/{claimId}/evidence") //A
    public ResponseEntity<?> uploadEvidence(@PathVariable long holderId , @PathVariable long claimId , @RequestParam("file") MultipartFile file)
    {
        boolean success;
        try {
            success = evidenceService.uploadfile(holderId, claimId, file);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(new ResponseDTO(e.getMessage()));
        }
        if (success)
            return ResponseEntity.ok().body(new ResponseDTO("Evidence Upload Successful"));

        return ResponseEntity.status(404).body(new ResponseDTO("Evidence Upload Failed !!!"));
    }

    @PreAuthorize("hasAuthority('CLAIMS_ADJUSTER')")
    @GetMapping("/{claimId}/evidence") //A
    public ResponseEntity<?> getEvidenceByClaimId(@PathVariable Long claimId){
        List<EvidenceDTO> evidence = evidenceService.getEvidenceByClaimId(claimId);
        if(evidence.isEmpty())
        {
            return ResponseEntity.ok().body(new ResponseDTO("No Evidence Found"));
        }
        return ResponseEntity.ok().body(evidence);
    }

    // code for getting the evidence as file
//    @GetMapping("/{claimId}/{evidenceId}/evidence")
//    public ResponseEntity<?> getEvidence(@PathVariable long holderId,@PathVariable long claimId,@PathVariable long evidenceId) {
//        byte[] fileBytes;
//        String fileName , fileType;
//        try
//        {
//            Optional<Evidence> metadata = evidenceService.getEvidenceMetadata(holderId , claimId , evidenceId);
//            fileBytes = evidenceService.getEvidences(holderId, claimId , evidenceId);
//            fileName = metadata.get().getFileName();
//            fileType = metadata.get().getFileType();
//            System.out.println(fileName);
//        }
//        catch(Exception e)
//        {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//        if (fileBytes.length == 0) {
//            return ResponseEntity.badRequest().body("No Evidence Found");
//        }
//        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=\"" + fileName + "\"" ).header(HttpHeaders.CONTENT_LENGTH , String.valueOf(fileBytes.length)).body(fileBytes);
//    }

    @PreAuthorize("hasAuthority('CLAIMS_ADJUSTER')")
    @PutMapping("/{claimId}/{evidenceId}/verify") //A
    public EvidenceDTO verifyEvidence(@PathVariable Long claimId, @PathVariable Long evidenceId, @RequestBody VerifyevidenceRequestDTO dto){
        return evidenceService.verifyEvidence(claimId,evidenceId,dto);
    }
}

