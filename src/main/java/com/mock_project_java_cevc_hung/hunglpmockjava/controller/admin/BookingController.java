package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.AdminPageRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.BookingUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.BookingResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    // Constants
    private static final String BASE_PATH = "/admin/bookings";
    private static final String VIEW_BASE = "admin/bookings/";
    private static final String VIEW_INDEX = VIEW_BASE + "index";
    private static final String VIEW_EDIT = VIEW_BASE + "edit";
    private static final String REDIRECT_BOOKINGS = "redirect:" + BASE_PATH;

    private final BookingService bookingService;
    private final MessageSource messageSource;

    @Autowired
    public BookingController(BookingService bookingService, MessageSource messageSource) {
        this.bookingService = bookingService;
        this.messageSource = messageSource;
    }
    
    private void addErrorFlash(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(AdminConstants.ATTR_ERROR, message);
    }
    
    private void addSuccessFlash(RedirectAttributes redirectAttributes, String message) {
        redirectAttributes.addFlashAttribute(AdminConstants.ATTR_SUCCESS, message);
    }
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
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
            logger.error("Error loading edit booking page for id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_LOAD_ERROR, e.getMessage()));
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
                logger.error("Error loading edit booking page for id {}: {}", id, e.getMessage(), e);
                addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_LOAD_ERROR, e.getMessage()));
                return REDIRECT_BOOKINGS;
            }
        }
        
        try {
            bookingService.updateBooking(id, request);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_UPDATE_SUCCESS));
            return REDIRECT_BOOKINGS;
        } catch (Exception e) {
            logger.error("Error updating booking id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_UPDATE_ERROR, e.getMessage()));
            return REDIRECT_BOOKINGS + "/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.deleteBooking(id);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_DELETE_SUCCESS));
        } catch (Exception e) {
            logger.error("Error deleting booking id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_DELETE_ERROR, e.getMessage()));
        }
        return REDIRECT_BOOKINGS;
    }

    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookingService.cancelBooking(id);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_CANCEL_SUCCESS));
        } catch (Exception e) {
            logger.error("Error cancelling booking id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_CANCEL_ERROR, e.getMessage()));
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
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_STATUS_UPDATE_SUCCESS));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid status value: {}", status);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_STATUS_INVALID, status));
        } catch (Exception e) {
            logger.error("Error updating booking status id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_BOOKING_STATUS_UPDATE_ERROR, e.getMessage()));
        }
        return REDIRECT_BOOKINGS;
    }
}
