package com.cts.claimbridge.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class EvidenceDTO { //A
    private Long evidenceId;
    private String fileName;
    private String fileType;
    private String filePath;
    private Boolean isVerified;
    private LocalDateTime uploadedAt;
}
