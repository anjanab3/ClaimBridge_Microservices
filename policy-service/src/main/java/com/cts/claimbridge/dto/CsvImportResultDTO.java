package com.cts.claimbridge.dto;

import lombok.Data;
import java.util.List;

@Data
public class CsvImportResultDTO {
    private int totalRows;
    private int holdersCreated;
    private int holdersReused;
    private int policiesCreated;
    private int policiesSkipped;
    private List<String> errors;
}
