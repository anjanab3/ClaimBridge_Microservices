package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.*;
import com.cts.claimbridge.repository.CommunicationRepository;
import com.cts.claimbridge.repository.InvestigationNoteRepository;
import com.cts.claimbridge.repository.InvestigationRepository;
import com.cts.claimbridge.repository.TriageDecisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InteractionService {
    @Autowired
    private InvestigationRepository invRepo;
    @Autowired
    private InvestigationNoteRepository noteRepo;
    @Autowired
    private TriageDecisionRepository triageRepo;
    @Autowired
    private CommunicationRepository commRepo;

    public InvestigationNote addNote(Long investigationId,@RequestBody InvestigationNoteRequestDTO dto) { //A

        Investigation investigation = invRepo.findById(investigationId)
                .orElseThrow(() -> new RuntimeException("Investigation not found"));

        // Get claim for this investigation
        Claim claim = investigation.getClaim();

        // Find triage decision for that claim
        List<TriageDecision> decision = triageRepo.findByClaim_ClaimId(claim.getClaimId());
        if(decision==null){
            throw new RuntimeException("Triage decision not found");
        }
        String adjusterId = decision.get(0).getAssignedTo();
        InvestigationNote note = new InvestigationNote();
        note.setAuthorId(adjusterId);
        note.setNoteText(dto.getNoteText());
        note.setInvestigation(investigation);
        note.setCreatedAt(LocalDateTime.now());
        return noteRepo.save(note);
    }
    public InvestigationNote updateNote(Long investigationId, InvNoteUpdateResponseDTO dto){ //A
        Investigation inv=invRepo.findById(investigationId)
                .orElseThrow(()->new RuntimeException("Investigation not found"));
        InvestigationNote note=noteRepo.findByNoteIdAndInvestigation_InvestigationId(dto.getNoteId(),investigationId)
                .orElseThrow(()->new RuntimeException("Note does not belong to this investigation"));
        String existing = note.getNoteText();
        String updated = (existing == null || existing.isEmpty())
                ? dto.getNoteText()
                : existing + "\n" + dto.getNoteText();
        note.setNoteText(updated);
        return noteRepo.save(note);
    }
}

