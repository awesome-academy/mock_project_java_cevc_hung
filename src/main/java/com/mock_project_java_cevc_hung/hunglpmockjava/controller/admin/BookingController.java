package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.AdminPageRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.BookingUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.BookingResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.BookingService;
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

@Controller
@RequestMapping("admin/bookings")
public class BookingController {

    // Constants
    private static final String BASE_PATH = "/admin/bookings";
    private static final String VIEW_BASE = "admin/bookings/";
    private static final String VIEW_INDEX = VIEW_BASE + "index";
    private static final String VIEW_EDIT = VIEW_BASE + "edit";
    private static final String REDIRECT_BOOKINGS = "redirect:" + BASE_PATH;

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("")
    public String adminBookingPage(
        @ModelAttribute AdminPageRequest request,
        Model model
    ) {
        Sort sort = request.getSortDir().equalsIgnoreCase("desc") ?
                Sort.by(request.getSortBy()).descending() : Sort.by(request.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<BookingResponse> bookings = bookingService.getAllBookings(request.getSearch(), pageable);

        model.addAttribute("bookings", bookings);
        model.addAttribute("currentPage", request.getPage());
        model.addAttribute("totalPages", bookings.getTotalPages());
        model.addAttribute("totalItems", bookings.getTotalElements());
        model.addAttribute("sortBy", request.getSortBy());
        model.addAttribute("sortDir", request.getSortDir());
        model.addAttribute("search", request.getSearch());
        model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_BOOKINGS);

        return VIEW_INDEX;
    }

    @GetMapping("/{id}/edit")
    public String editBookingPage(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            BookingResponse booking = bookingService.getBookingById(id);
            
            BookingUpdateRequest updateRequest = BookingUpdateRequest.builder()
                .qty(booking.getQty())
                .amount(booking.getAmount())
                .build();
            
            model.addAttribute("bookingUpdateRequest", updateRequest);
            model.addAttribute("booking", booking);
            model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_BOOKINGS);
            return VIEW_EDIT;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error loading booking: " + e.getMessage());
            return REDIRECT_BOOKINGS;
        }
    }

    @PostMapping("/{id}/edit")
    public String updateBooking(
        @PathVariable Long id,
        @Valid @ModelAttribute BookingUpdateRequest request,
        BindingResult result,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        if (result.hasErrors()) {
            try {
                BookingResponse booking = bookingService.getBookingById(id);
                model.addAttribute("booking", booking);
                model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_BOOKINGS);
                return VIEW_EDIT;
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error loading booking: " + e.getMessage());
                return REDIRECT_BOOKINGS;
            }
        }
        
        try {
            bookingService.updateBooking(id, request);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Booking updated successfully!");
            return REDIRECT_BOOKINGS;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error updating booking: " + e.getMessage());
            return REDIRECT_BOOKINGS + "/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.deleteBooking(id);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Booking deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error deleting booking: " + e.getMessage());
        }
        return REDIRECT_BOOKINGS;
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error cancelling booking: " + e.getMessage());
        }
        return REDIRECT_BOOKINGS;
    }

    @PostMapping("/{id}/status")
    public String changeBookingStatus(
        @PathVariable Long id,
        @RequestParam("status") String status,
        RedirectAttributes redirectAttributes
    ) {
        try {
            BookingEntity.Status newStatus = BookingEntity.Status.valueOf(status.toUpperCase());
            bookingService.toggleBookingStatus(id, newStatus);
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, "Booking status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, "Error updating booking status: " + e.getMessage());
        }
        return REDIRECT_BOOKINGS;
    }
}
