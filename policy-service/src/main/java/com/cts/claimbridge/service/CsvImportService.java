package com.cts.claimbridge.service;

import com.cts.claimbridge.dto.CsvImportResultDTO;
import com.cts.claimbridge.entity.Policy;
import com.cts.claimbridge.entity.PolicyHolder;
import com.cts.claimbridge.repository.PolicyHolderRepository;
import com.cts.claimbridge.repository.PolicyRepository;
import com.cts.claimbridge.util.PolicyStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvImportService {

    private final PolicyHolderRepository holderRepo;
    private final PolicyRepository       policyRepo;

    /**
     * Expected CSV columns (first row = header, ignored):
     *
     * holderName, contactInfo, businessType, taxId,
     * policyNumber, insuredName, effectiveDate, expiryDate,
     * status, coverageType, coverageLimit, deductible
     */
    public CsvImportResultDTO importCsv(MultipartFile file) throws Exception {

        CsvImportResultDTO result = new CsvImportResultDTO();
        List<String> errors = new ArrayList<>();
        int holdersCreated = 0, holdersReused = 0, policiesCreated = 0, policiesSkipped = 0;
        int rowNum = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean firstLine = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                // Skip header row
                if (firstLine) { firstLine = false; continue; }

                rowNum++;
                try {
                    String[] cols = parseCsvLine(line);

                    if (cols.length < 12) {
                        errors.add("Row " + rowNum + ": expected 12 columns, got " + cols.length);
                        continue;
                    }

                    String holderName   = col(cols, 0);
                    String contactInfo  = col(cols, 1);
                    String businessType = col(cols, 2);
                    String taxId        = col(cols, 3);
                    String policyNumber = col(cols, 4);
                    String insuredName  = col(cols, 5);
                    String effectiveStr = col(cols, 6);
                    String expiryStr    = col(cols, 7);
                    String statusStr    = col(cols, 8);
                    String coverageType = col(cols, 9);
                    String limitStr     = col(cols, 10);
                    String deductStr    = col(cols, 11);

                    // Validate required fields
                    if (holderName.isEmpty())   { errors.add("Row " + rowNum + ": holderName is required");   continue; }
                    if (policyNumber.isEmpty())  { errors.add("Row " + rowNum + ": policyNumber is required"); continue; }
                    if (insuredName.isEmpty())   { errors.add("Row " + rowNum + ": insuredName is required");  continue; }

                    // Skip duplicate policy numbers
                    if (policyRepo.existsByPolicyNumber(policyNumber)) {
                        policiesSkipped++;
                        errors.add("Row " + rowNum + ": policy " + policyNumber + " already exists — skipped");
                        continue;
                    }

                    // ── Resolve or create PolicyHolder ──────────────────────────────
                    PolicyHolder holder = resolveHolder(holderName, contactInfo, businessType, taxId);
                    if (holder.getHolderId() == null) {
                        holder = holderRepo.save(holder);
                        holdersCreated++;
                        log.info("Created holder '{}' (id={})", holder.getName(), holder.getHolderId());
                    } else {
                        holdersReused++;
                    }

                    // ── Build coverageJSON ───────────────────────────────────────────
                    double coverageLimit = parseDouble(limitStr, 0);
                    double deductible    = parseDouble(deductStr, 0);
                    String coverageJson  = buildCoverageJson(coverageType, coverageLimit, deductible);

                    // ── Build and save Policy ────────────────────────────────────────
                    Policy policy = new Policy();
                    policy.setPolicyNumber(policyNumber);
                    policy.setInsuredName(insuredName.isEmpty() ? holderName : insuredName);
                    policy.setEffectiveDate(parseDate(effectiveStr));
                    policy.setExpiryDate(parseDate(expiryStr));
                    policy.setStatus(parseStatus(statusStr));
                    policy.setCoverageJSON(coverageJson);
                    policy.setHolderId(holder.getHolderId());

                    policyRepo.save(policy);
                    policiesCreated++;
                    log.info("Created policy '{}' for holder id={}", policyNumber, holder.getHolderId());

                } catch (Exception e) {
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                }
            }
        }

        result.setTotalRows(rowNum);
        result.setHoldersCreated(holdersCreated);
        result.setHoldersReused(holdersReused);
        result.setPoliciesCreated(policiesCreated);
        result.setPoliciesSkipped(policiesSkipped);
        result.setErrors(errors);
        return result;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Find an existing holder by taxId (preferred) or exact name match.
     * Returns an unsaved holder (holderId == null) if none found.
     */
    private PolicyHolder resolveHolder(String name, String contactInfo,
                                       String businessType, String taxId) {
        // Try taxId first (most reliable unique key)
        if (!taxId.isEmpty()) {
            Optional<PolicyHolder> byTax = holderRepo.findByTaxID(taxId);
            if (byTax.isPresent()) return byTax.get();
        }

        // Fall back to exact name match
        Optional<PolicyHolder> byName = holderRepo.findByName(name);
        if (byName.isPresent()) return byName.get();

        // Build a new (unsaved) holder
        PolicyHolder h = new PolicyHolder();
        h.setName(name);
        h.setContactInfo(contactInfo);
        h.setBusinessType(businessType);
        h.setTaxID(taxId.isEmpty() ? null : taxId);
        return h;
    }

    private String buildCoverageJson(String type, double limit, double deductible) {
        return String.format(
            "{\"type\":\"%s\",\"coverageLimit\":%.2f,\"deductible\":%.2f}",
            type.isEmpty() ? "General" : type, limit, deductible);
    }

    /** Split a CSV line respecting double-quoted fields that may contain commas. */
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        tokens.add(cur.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private String col(String[] cols, int i) {
        return (i < cols.length) ? cols[i].trim() : "";
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s.trim()); } catch (Exception e) { return null; }
    }

    private PolicyStatus parseStatus(String s) {
        try { return PolicyStatus.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return PolicyStatus.ACTIVE; }
    }

    private double parseDouble(String s, double fallback) {
        try { return Double.parseDouble(s.trim()); } catch (Exception e) { return fallback; }
    }
}
