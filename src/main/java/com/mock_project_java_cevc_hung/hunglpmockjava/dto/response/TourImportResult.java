package com.mock_project_java_cevc_hung.hunglpmockjava.dto.response;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TourImportResult {
    
    private int totalRows;
    
    private int successCount;
    
    private int errorCount;
    
    @Builder.Default
    private List<ImportError> errors = new ArrayList<>();
    
    public void addError(int rowNumber, String message) {
        errors.add(new ImportError(rowNumber, message));
        errorCount++;
    }
    
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ImportError {
        private int rowNumber;
        private String message;
    }
}
