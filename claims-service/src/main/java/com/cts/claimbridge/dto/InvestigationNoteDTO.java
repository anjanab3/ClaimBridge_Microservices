package com.cts.claimbridge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class InvestigationNoteDTO { //A
    private Long noteId;
    private String authorId;
    private String noteText;
    private LocalDateTime createdAt;
}
