package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.AdminPageRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.CategoryRevenueDTO;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.MonthlyRevenueDTO;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.RevenueResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TopRatedTourDTO;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.RevenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/revenue")
public class RevenueController {

    private static final Logger logger = LoggerFactory.getLogger(RevenueController.class);
    private static final int MONTHS_IN_CHART = 6;
    private static final int TOP_CATEGORY_LIMIT = 5;
    private static final int TOP_RATED_TOUR_LIMIT = 5;

    private final RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @GetMapping("")
    public String revenuePage(
        @ModelAttribute AdminPageRequest request,
        Model model
    ) {
        Sort sort = request.getSortDir().equalsIgnoreCase("desc") ?
                Sort.by(request.getSortBy()).descending() : Sort.by(request.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        try {
            Page<RevenueResponse> revenues = revenueService.getAllRevenues(request.getSearch(), pageable);
            Double totalRevenue = revenueService.getTotalRevenue();
            List<MonthlyRevenueDTO> chartData = revenueService.getRevenueChartData(MONTHS_IN_CHART);
            List<String> chartLabels = chartData.stream()
                    .map(MonthlyRevenueDTO::getLabel)
                    .collect(Collectors.toList());
            List<Double> chartValues = chartData.stream()
                    .map(MonthlyRevenueDTO::getTotalRevenue)
                    .collect(Collectors.toList());
            List<Integer> chartBookings = chartData.stream()
                    .map(dto -> dto.getTotalBookings() != null ? dto.getTotalBookings() : 0)
                    .collect(Collectors.toList());
            List<CategoryRevenueDTO> categoryRevenues = revenueService.getTopCategoriesByRevenue(TOP_CATEGORY_LIMIT);
            List<TopRatedTourDTO> topRatedTours = revenueService.getTopRatedTours(TOP_RATED_TOUR_LIMIT);
            List<String> categoryLabels = categoryRevenues.stream()
                    .map(CategoryRevenueDTO::getLabel)
                    .collect(Collectors.toList());
            List<Double> categoryValues = categoryRevenues.stream()
                    .map(dto -> dto.getTotalRevenue() != null ? dto.getTotalRevenue() : 0.0)
                    .collect(Collectors.toList());
            List<Integer> categoryBookings = categoryRevenues.stream()
                    .map(dto -> dto.getTotalBookings() != null ? dto.getTotalBookings() : 0)
                    .collect(Collectors.toList());
            List<String> topTourLabels = topRatedTours.stream()
                    .map(TopRatedTourDTO::getLabel)
                    .collect(Collectors.toList());
            List<Double> topTourRatings = topRatedTours.stream()
                    .map(dto -> dto.getAverageRating() != null ? dto.getAverageRating() : 0.0)
                    .collect(Collectors.toList());
            List<Long> topTourReviews = topRatedTours.stream()
                    .map(dto -> dto.getTotalReviews() != null ? dto.getTotalReviews() : 0L)
                    .collect(Collectors.toList());

            if (revenues == null) {
                logger.warn("Revenue list is null, defaulting to empty page");
                revenues = Page.empty(pageable);
            }
            if (totalRevenue == null) {
                logger.warn("Total revenue is null, defaulting to 0.0");
                totalRevenue = 0.0;
            }

            model.addAttribute("revenues", revenues);
            model.addAttribute("totalRevenue", totalRevenue);
            model.addAttribute("revenueChartLabels", chartLabels);
            model.addAttribute("revenueChartValues", chartValues);
            model.addAttribute("revenueChartBookings", chartBookings);
            model.addAttribute("currentPage", request.getPage());
            model.addAttribute("totalPages", revenues.getTotalPages());
            model.addAttribute("totalItems", revenues.getTotalElements());
            model.addAttribute("sortBy", request.getSortBy());
            model.addAttribute("sortDir", request.getSortDir());
            model.addAttribute("search", request.getSearch());
            model.addAttribute("categoryRevenues", categoryRevenues);
            model.addAttribute("topRatedTours", topRatedTours);
            model.addAttribute("categoryRevenueLabels", categoryLabels);
            model.addAttribute("categoryRevenueValues", categoryValues);
            model.addAttribute("categoryRevenueBookings", categoryBookings);
            model.addAttribute("topRatedTourLabels", topTourLabels);
            model.addAttribute("topRatedTourRatings", topTourRatings);
            model.addAttribute("topRatedTourReviews", topTourReviews);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_REVENUE);

            return AdminConstants.VIEW_INDEX_REVENUE;
        } catch (RuntimeException ex) {
            logger.error("Failed to load revenue page: {}", ex.getMessage(), ex);
            model.addAttribute(AdminConstants.ATTR_ERROR, "Unable to load revenue data");
            model.addAttribute("revenues", Page.empty(pageable));
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute("revenueChartLabels", Collections.emptyList());
            model.addAttribute("revenueChartValues", Collections.emptyList());
            model.addAttribute("revenueChartBookings", Collections.emptyList());
            model.addAttribute("categoryRevenues", Collections.emptyList());
            model.addAttribute("topRatedTours", Collections.emptyList());
            model.addAttribute("categoryRevenueLabels", Collections.emptyList());
            model.addAttribute("categoryRevenueValues", Collections.emptyList());
            model.addAttribute("categoryRevenueBookings", Collections.emptyList());
            model.addAttribute("topRatedTourLabels", Collections.emptyList());
            model.addAttribute("topRatedTourRatings", Collections.emptyList());
            model.addAttribute("topRatedTourReviews", Collections.emptyList());
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_REVENUE);
            return AdminConstants.VIEW_INDEX_REVENUE;
        }
    }

    @GetMapping("/export/monthly")
    public ResponseEntity<byte[]> exportMonthlyRevenue() {
        try {
            String csv = revenueService.exportMonthlyRevenueToCSV(MONTHS_IN_CHART);
            byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "revenue_monthly.csv");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
        } catch (Exception e) {
            logger.error("Failed to export monthly revenue: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/categories")
    public ResponseEntity<byte[]> exportCategoryRevenue() {
        try {
            String csv = revenueService.exportCategoryRevenueToCSV(TOP_CATEGORY_LIMIT);
            byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "revenue_by_category.csv");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
        } catch (Exception e) {
            logger.error("Failed to export category revenue: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/all")
    public ResponseEntity<byte[]> exportAllRevenues() {
        try {
            String csv = revenueService.exportAllRevenuesToCSV();
            byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "all_revenues.csv");
            headers.setContentLength(bytes.length);

            return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
        } catch (Exception e) {
            logger.error("Failed to export all revenues: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

