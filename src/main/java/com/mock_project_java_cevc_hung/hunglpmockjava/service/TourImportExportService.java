package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.TourImportRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TourImportResult;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CategoryEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.TourRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TourImportExportService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private Validator validator;

    private static final String CSV_HEADER = "ID,Title,Description,Price,Location,Thumbnail URL,Seats Total,Seats Available,Start Date,End Date,Status,Category Name,Rating Average";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int EXPECTED_CSV_COLUMNS = 13;

    /**
     * Export all tours to CSV format
     */
    public String exportToursToCSV() {
        List<TourEntity> tours = tourRepository.findAll();
        StringBuilder csv = new StringBuilder();

        csv.append(CSV_HEADER).append("\n");

        for (TourEntity tour : tours) {
            csv.append(escapeCsv(tour.getId() != null ? tour.getId().toString() : "")).append(",");
            csv.append(escapeCsv(tour.getTitle())).append(",");
            csv.append(escapeCsv(tour.getDescription())).append(",");
            csv.append(escapeCsv(tour.getPrice() != null ? tour.getPrice().toString() : "")).append(",");
            csv.append(escapeCsv(tour.getLocation())).append(",");
            csv.append(escapeCsv(tour.getThumbnailUrl() != null ? tour.getThumbnailUrl() : "")).append(",");
            csv.append(escapeCsv(tour.getSeatsTotal() != null ? tour.getSeatsTotal().toString() : "")).append(",");
            csv.append(escapeCsv(tour.getSeatsAvailable() != null ? tour.getSeatsAvailable().toString() : ""))
                    .append(",");
            csv.append(escapeCsv(tour.getStartDate() != null ? tour.getStartDate().format(DATE_FORMATTER) : ""))
                    .append(",");
            csv.append(escapeCsv(tour.getEndDate() != null ? tour.getEndDate().format(DATE_FORMATTER) : ""))
                    .append(",");
            csv.append(escapeCsv(tour.getStatus() != null ? tour.getStatus().name() : "")).append(",");
            csv.append(escapeCsv(tour.getCategory() != null ? tour.getCategory().getName() : "")).append(",");
            csv.append(escapeCsv(tour.getRatingAvg() != null ? tour.getRatingAvg().toString() : ""));
            csv.append("\n");
        }

        return csv.toString();
    }

    /**
     * Generate CSV template for import
     */
    public String generateCSVTemplate() {
        return CSV_HEADER + "\n";
    }

    /**
     * Import tours from CSV file
     */
    @Transactional
    public TourImportResult importToursFromCSV(MultipartFile file) {
        TourImportResult result = TourImportResult.builder()
                .totalRows(0)
                .successCount(0)
                .errorCount(0)
                .build();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                result.addError(0, "File is empty");
                return result;
            }

            // Load all categories once to avoid N+1 query
            Map<String, CategoryEntity> categoryMap = categoryRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            c -> c.getName().toLowerCase(),
                            c -> c,
                            (existing, replacement) -> existing));

            String line;
            int rowNumber = 1; // Start from 1 (after header)

            while ((line = reader.readLine()) != null) {
                rowNumber++;

                // Skip empty lines before counting
                if (line.trim().isEmpty()) {
                    continue;
                }

                result.setTotalRows(result.getTotalRows() + 1);

                try {
                    TourImportRequest importRequest = parseCsvLine(line, rowNumber);

                    // Validate
                    Set<ConstraintViolation<TourImportRequest>> violations = validator.validate(importRequest);
                    if (!violations.isEmpty()) {
                        StringBuilder errors = new StringBuilder();
                        for (ConstraintViolation<TourImportRequest> violation : violations) {
                            errors.append(violation.getMessage()).append("; ");
                        }
                        result.addError(rowNumber, errors.toString());
                        continue;
                    }

                    // Custom validation: seatsAvailable <= seatsTotal
                    if (!importRequest.isSeatsValid()) {
                        result.addError(rowNumber, "Available seats cannot exceed total seats");
                        continue;
                    }

                    // Custom validation: endDate should be after startDate
                    if (importRequest.getStartDate() != null && importRequest.getEndDate() != null) {
                        if (importRequest.getEndDate().isBefore(importRequest.getStartDate())) {
                            result.addError(rowNumber, "End date must be after start date");
                            continue;
                        }
                    }

                    // Find category by name using pre-loaded map
                    CategoryEntity category = categoryMap.get(importRequest.getCategoryName().trim().toLowerCase());

                    if (category == null) {
                        result.addError(rowNumber, "Category '" + importRequest.getCategoryName() + "' not found");
                        continue;
                    }

                    // Create tour entity
                    TourEntity tour = TourEntity.builder()
                            .title(importRequest.getTitle())
                            .description(importRequest.getDescription())
                            .price(importRequest.getPrice())
                            .location(importRequest.getLocation())
                            .thumbnailUrl(importRequest.getThumbnailUrl())
                            .seatsTotal(importRequest.getSeatsTotal())
                            .seatsAvailable(
                                    importRequest.getSeatsAvailable() != null ? importRequest.getSeatsAvailable()
                                            : importRequest.getSeatsTotal())
                            .startDate(importRequest.getStartDate())
                            .endDate(importRequest.getEndDate())
                            .status(parseStatus(importRequest.getStatus()))
                            .category(category)
                            .ratingAvg(importRequest.getRatingAvg() != null ? importRequest.getRatingAvg() : 0.0)
                            .build();

                    tourRepository.save(tour);
                    result.incrementSuccessCount();

                } catch (Exception e) {
                    result.addError(rowNumber, "Error: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            result.addError(0, "Failed to read file: " + e.getMessage());
        }

        return result;
    }

    /**
     * Parse CSV line into TourImportRequest
     */
    private TourImportRequest parseCsvLine(String line, int rowNumber) throws Exception {
        List<String> values = parseCsvRow(line);

        if (values.size() < EXPECTED_CSV_COLUMNS) {
            throw new Exception(
                    "Invalid CSV format: expected " + EXPECTED_CSV_COLUMNS + " columns, found " + values.size());
        }

        TourImportRequest request = new TourImportRequest();

        try {
            // Skip ID (column 0) as we're only doing INSERT
            request.setTitle(values.get(1).trim());
            request.setDescription(values.get(2).trim());
            request.setPrice(values.get(3).trim().isEmpty() ? null : new BigDecimal(values.get(3).trim()));
            request.setLocation(values.get(4).trim());
            request.setThumbnailUrl(values.get(5).trim().isEmpty() ? null : values.get(5).trim());
            request.setSeatsTotal(values.get(6).trim().isEmpty() ? null : Integer.parseInt(values.get(6).trim()));
            request.setSeatsAvailable(values.get(7).trim().isEmpty() ? null : Integer.parseInt(values.get(7).trim()));
            request.setStartDate(
                    values.get(8).trim().isEmpty() ? null : LocalDateTime.parse(values.get(8).trim(), DATE_FORMATTER));
            request.setEndDate(
                    values.get(9).trim().isEmpty() ? null : LocalDateTime.parse(values.get(9).trim(), DATE_FORMATTER));
            request.setStatus(values.get(10).trim());
            request.setCategoryName(values.get(11).trim());
            request.setRatingAvg(values.get(12).trim().isEmpty() ? null : Double.parseDouble(values.get(12).trim()));

        } catch (NumberFormatException e) {
            throw new Exception("Invalid number format: " + e.getMessage());
        } catch (DateTimeParseException e) {
            throw new Exception("Invalid date format (expected: yyyy-MM-dd HH:mm:ss): " + e.getMessage());
        }

        return request;
    }

    /**
     * Parse CSV row handling quoted fields and escaped quotes
     */
    private List<String> parseCsvRow(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                // Check for escaped quote ""
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentValue.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        values.add(currentValue.toString());

        return values;
    }

    /**
     * Escape CSV special characters and prevent CSV injection
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        // Prevent CSV injection by prefixing formula characters with single quote
        if (value.startsWith("=") || value.startsWith("+") ||
                value.startsWith("-") || value.startsWith("@")) {
            value = "'" + value;
        }

        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Parse status string to enum
     */
    private TourEntity.Status parseStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return TourEntity.Status.ACTIVE;
        }
        try {
            return TourEntity.Status.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TourEntity.Status.ACTIVE;
        }
    }
}
