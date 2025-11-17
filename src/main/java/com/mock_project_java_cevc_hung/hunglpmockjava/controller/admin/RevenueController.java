package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.AdminPageRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.RevenueResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.RevenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/revenue")
public class RevenueController {

    private static final Logger logger = LoggerFactory.getLogger(RevenueController.class);

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
            model.addAttribute("currentPage", request.getPage());
            model.addAttribute("totalPages", revenues.getTotalPages());
            model.addAttribute("totalItems", revenues.getTotalElements());
            model.addAttribute("sortBy", request.getSortBy());
            model.addAttribute("sortDir", request.getSortDir());
            model.addAttribute("search", request.getSearch());
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_REVENUE);

            return AdminConstants.VIEW_INDEX_REVENUE;
        } catch (RuntimeException ex) {
            logger.error("Failed to load revenue page: {}", ex.getMessage(), ex);
            model.addAttribute(AdminConstants.ATTR_ERROR, "Unable to load revenue data");
            model.addAttribute("revenues", Page.empty(pageable));
            model.addAttribute("totalRevenue", 0.0);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_REVENUE);
            return AdminConstants.VIEW_INDEX_REVENUE;
        }
    }
}

