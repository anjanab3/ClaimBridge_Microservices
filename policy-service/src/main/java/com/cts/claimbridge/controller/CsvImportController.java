package com.cts.claimbridge.controller;

import com.cts.claimbridge.dto.CsvImportResultDTO;
import com.cts.claimbridge.service.CsvImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
public class CsvImportController {

    private final CsvImportService csvImportService;

    /**
     * POST /api/admin/import/csv
     * Accepts a multipart CSV file and bulk-imports policyholders + policies.
     */
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/csv")
    public ResponseEntity<?> importCsv(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file provided");
        }
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        if (!filename.toLowerCase().endsWith(".csv")) {
            return ResponseEntity.badRequest().body("Only .csv files are accepted");
        }
        try {
            CsvImportResultDTO result = csvImportService.importCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Import failed: " + e.getMessage());
        }
    }
}
