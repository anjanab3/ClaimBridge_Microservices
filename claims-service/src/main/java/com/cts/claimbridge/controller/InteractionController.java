package com.cts.claimbridge.controller;


import com.cts.claimbridge.dto.*;
import com.cts.claimbridge.entity.InvestigationNote;
import com.cts.claimbridge.service.InteractionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/investigations")
public class InteractionController {
    @Autowired
    private InteractionService service;

    @PostMapping("/{investigationId}/notes") //A
    public ResponseEntity<?> addNote(@PathVariable Long investigationId,@RequestBody InvestigationNoteRequestDTO dto) {
        try {
            return ResponseEntity.ok().body(new MessageDTO(service.addNote(investigationId, dto),"Investigation note added successfully!!!"));
        }
        catch(Exception e)
        {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping ("{investigationId}/notes") //A
    public ResponseEntity<?> updateNote(@PathVariable Long investigationId, @RequestBody InvNoteUpdateResponseDTO dto) {
        try {
            return ResponseEntity.ok().body(new MessageDTO(service.updateNote(investigationId, dto),"Fraud data updated successfully !!!"));
        }
        catch(Exception e)
        {
            return ResponseEntity.badRequest().body(new ResponseDTO("No investigation Id found"));
        }
    }
}