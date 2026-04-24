package com.cts.claimbridge.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestigationFullResponseDTO { //A
    private Long investigationId;
    private String status;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private List<InvestigationNoteDTO> investigationNotes;
}