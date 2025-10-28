package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.TourCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.TourUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TourResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CategoryEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.TourService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class TourController {

    @Autowired
    private TourService tourService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/tours")
    public String toursPage(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search,
        Model model
    ) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        TourEntity.Status tourStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                tourStatus = TourEntity.Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, use null
            }
        }
        
        Page<TourResponse> tours = tourService.getToursWithFilters(tourStatus, search, pageable);
        List<CategoryEntity> categories = categoryRepository.findAll();
        
        model.addAttribute("tours", tours);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", tours.getTotalPages());
        model.addAttribute("totalItems", tours.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        model.addAttribute("status", status);
        model.addAttribute("search", search);
        model.addAttribute("activePage", "tours");
        
        return "admin/tours/index";
    }

    @GetMapping("/tours/create")
    public String createTourPage(Model model) {
        List<CategoryEntity> categories = categoryRepository.findAll();
        model.addAttribute("tourCreateRequest", new TourCreateRequest());
        model.addAttribute("categories", categories);
        model.addAttribute("activePage", "tours");
        return "admin/tours/tour-create";
    }

    @PostMapping("/tours/create")
    public String createTour(
            @Valid @ModelAttribute TourCreateRequest request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            List<CategoryEntity> categories = categoryRepository.findAll();
            model.addAttribute("categories", categories);
            model.addAttribute("activePage", "tours");
            return "admin/tours/tour-create";
        }
        
        try {
            tourService.createTour(request);
            redirectAttributes.addFlashAttribute("success", "Tour created successfully!");
            return "redirect:/admin/tours";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating tour: " + e.getMessage());
            return "redirect:/admin/tours/create";
        }
    }

    @GetMapping("/tours/{id}/edit")
    public String editTourPage(@PathVariable Long id, Model model) {
        try {
            TourResponse tour = tourService.getTourById(id);
            List<CategoryEntity> categories = categoryRepository.findAll();
            
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
            model.addAttribute("activePage", "tours");
            return "admin/tours/tour-edit";
        } catch (Exception e) {
            return "redirect:/admin/tours?error=" + e.getMessage();
        }
    }

    @PostMapping("/tours/{id}/edit")
    public String updateTour(@PathVariable Long id,
                           @Valid @ModelAttribute TourUpdateRequest request,
                           BindingResult result,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        
        if (result.hasErrors()) {
            try {
                TourResponse tour = tourService.getTourById(id);
                List<CategoryEntity> categories = categoryRepository.findAll();
                model.addAttribute("tour", tour);
                model.addAttribute("categories", categories);
                model.addAttribute("activePage", "tours");
                return "admin/tours/tour-edit";
            } catch (Exception e) {
                return "redirect:/admin/tours?error=" + e.getMessage();
            }
        }
        
        try {
            tourService.updateTour(id, request);
            redirectAttributes.addFlashAttribute("success", "Tour updated successfully!");
            return "redirect:/admin/tours";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating tour: " + e.getMessage());
            return "redirect:/admin/tours/" + id + "/edit";
        }
    }

    @PostMapping("/tours/{id}/delete")
    public String deleteTour(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tourService.deleteTour(id);
            redirectAttributes.addFlashAttribute("success", "Tour deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting tour: " + e.getMessage());
        }
        return "redirect:/admin/tours";
    }

    @PostMapping("/tours/{id}/toggle-status")
    public String toggleTourStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            tourService.toggleTourStatus(id);
            redirectAttributes.addFlashAttribute("success", "Tour status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error updating tour status: " + e.getMessage());
        }
        return "redirect:/admin/tours";
    }
}
