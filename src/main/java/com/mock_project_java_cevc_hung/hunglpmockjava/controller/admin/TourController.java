package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.AdminPageRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.TourCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.TourUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TourImportResult;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TourResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.CategoryService;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.TourImportExportService;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.TourService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class TourController {

    // Constants
    private static final String BASE_PATH = "/admin/tours";
    private static final String VIEW_BASE = "admin/tours/";
    private static final String VIEW_INDEX = VIEW_BASE + "index";
    private static final String VIEW_CREATE = VIEW_BASE + "tour-create";
    private static final String VIEW_EDIT = VIEW_BASE + "tour-edit";
    private static final String REDIRECT_TOURS = "redirect:" + BASE_PATH;
    private static final String REDIRECT_CREATE = REDIRECT_TOURS + "/create";

    private static final Logger logger = LoggerFactory.getLogger(TourController.class);

    private final TourService tourService;
    private final CategoryService categoryService;
    private final TourImportExportService importExportService;

    @Autowired
    public TourController(
        TourService tourService,
        CategoryService categoryService,
        TourImportExportService importExportService
    ) {
        this.tourService = tourService;
        this.categoryService = categoryService;
        this.importExportService = importExportService;
    }

    @GetMapping("/tours")
    public String toursPage(
        @ModelAttribute AdminPageRequest request,
        Model model
    ) {

        Sort sort = request.getSortDir().equalsIgnoreCase("desc") ? Sort.by(request.getSortBy()).descending()
                : Sort.by(request.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        TourEntity.Status tourStatus = null;
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                tourStatus = TourEntity.Status.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, use null
            }
        }

        Page<TourResponse> tours = tourService.getToursWithFilters(tourStatus, request.getSearch(), pageable);
        List<CategoryResponse> categories = categoryService.getAllCategoriesAsList();

        model.addAttribute("tours", tours);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", request.getPage());
        model.addAttribute("totalPages", tours.getTotalPages());
        model.addAttribute("totalItems", tours.getTotalElements());
        model.addAttribute("sortBy", request.getSortBy());
        model.addAttribute("sortDir", request.getSortDir());
        model.addAttribute("status", request.getStatus());
        model.addAttribute("search", request.getSearch());
        model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_TOURS);

        return VIEW_INDEX;
    }

    @GetMapping("/tours/create")
    public String createTourPage(Model model) {
        List<CategoryResponse> categories = categoryService.getAllCategoriesAsList();
        model.addAttribute("tourCreateRequest", new TourCreateRequest());
        model.addAttribute("categories", categories);
        model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_TOURS);
        return VIEW_CREATE;
    }

    @PostMapping("/tours/create")
    public String createTour(
            @Valid @ModelAttribute TourCreateRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        if (result.hasErrors()) {
            List<CategoryResponse> categories = categoryService.getAllCategoriesAsList();
            model.addAttribute("categories", categories);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_TOURS);
            return VIEW_CREATE;
        }

        try {
            tourService.createTour(request);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Tour created successfully!");
            return REDIRECT_TOURS;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error creating tour: " + e.getMessage());
            return REDIRECT_CREATE;
        }
    }

    @GetMapping("/tours/{id}/edit")
    public String editTourPage(@PathVariable Long id, Model model) {
        try {
            TourResponse tour = tourService.getTourById(id);
            List<CategoryResponse> categories = categoryService.getAllCategoriesAsList();

            TourUpdateRequest updateRequest = TourUpdateRequest.builder()
                    .title(tour.getTitle())
                    .description(tour.getDescription())
                    .price(tour.getPrice())
                    .location(tour.getLocation())
                    .seatsTotal(tour.getSeatsTotal())
                    .seatsAvailable(tour.getSeatsAvailable())
                    .startDate(tour.getStartDate())
                    .endDate(tour.getEndDate())
                    .categoryId(tour.getCategoryId())
                    .build();

            model.addAttribute("tourUpdateRequest", updateRequest);
            model.addAttribute("tour", tour);
            model.addAttribute("categories", categories);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_TOURS);
            return VIEW_EDIT;
        } catch (Exception e) {
            return REDIRECT_TOURS + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/tours/{id}/edit")
    public String updateTour(
            @PathVariable Long id,
            @Valid @ModelAttribute TourUpdateRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            try {
                TourResponse tour = tourService.getTourById(id);
                List<CategoryResponse> categories = categoryService.getAllCategoriesAsList();
                model.addAttribute("tour", tour);
                model.addAttribute("categories", categories);
                model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_TOURS);
                return VIEW_EDIT;
            } catch (Exception e) {
                return REDIRECT_TOURS + "?error=" + e.getMessage();
            }
        }

        try {
            tourService.updateTour(id, request);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Tour updated successfully!");
            return REDIRECT_TOURS;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error updating tour: " + e.getMessage());
            return REDIRECT_TOURS + "/" + id + "/edit";
        }
    }

    @PostMapping("/tours/{id}/delete")
    public String deleteTour(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tourService.deleteTour(id);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Tour deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error deleting tour: " + e.getMessage());
        }
        return REDIRECT_TOURS;
    }

    @PostMapping("/tours/{id}/toggle-status")
    public String toggleTourStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tourService.toggleTourStatus(id);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Tour status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR,
                    "Error updating tour status: " + e.getMessage());
        }
        return REDIRECT_TOURS;
    }

    @GetMapping("/tours/export")
    public ResponseEntity<byte[]> exportTours() {
        try {
            String csv = importExportService.exportToursToCSV();
            byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "tours_export.csv");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tours/import-template")
    public ResponseEntity<byte[]> downloadImportTemplate() {
        try {
            String csv = importExportService.generateCSVTemplate();
            byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "tours_import_template.csv");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/tours/import")
    public String importToursPage(Model model) {
        model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_TOURS);
        return VIEW_BASE + "tour-import";
    }

    @PostMapping("/tours/import")
    public String importTours(
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Validate file
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Please select a file to upload");
            return "redirect:" + BASE_PATH + "/import";
        }

        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "File size exceeds maximum limit of 10MB");
            return "redirect:" + BASE_PATH + "/import";
        }

        // Check MIME type and filename
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Only CSV files are allowed");
            return "redirect:" + BASE_PATH + "/import";
        }

        if (contentType != null && !contentType.equals("text/csv") && !contentType.equals("application/vnd.ms-excel")) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR,
                    "Invalid file type. Only CSV files are allowed");
            return "redirect:" + BASE_PATH + "/import";
        }

        try {
            TourImportResult result = importExportService.importToursFromCSV(file);

            model.addAttribute("result", result);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_TOURS);

            if (result.getErrorCount() == 0) {
                model.addAttribute(AdminConstants.ATTR_SUCCESS,
                        "Import completed successfully! " + result.getSuccessCount() + " tours imported.");
            } else {
                model.addAttribute(AdminConstants.ATTR_WARNING,
                        "Import completed with errors. " + result.getSuccessCount() + " tours imported, " +
                                result.getErrorCount() + " failed.");
            }

            return VIEW_BASE + "tour-import";

        } catch (Exception e) {
            logger.error("Error importing tours", e);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR,
                    "Error importing tours: " + e.getMessage() + ". Please check the file format and try again.");
            return "redirect:" + BASE_PATH + "/import";
        }
    }
}
