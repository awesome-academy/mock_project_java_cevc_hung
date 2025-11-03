package com.mock_project_java_cevc_hung.hunglpmockjava.controller.admin;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.AdminPageRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.ReviewResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.ReviewService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class ReviewController {

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    // Constants
    private static final String BASE_PATH = "/admin/reviews";
    private static final String VIEW_BASE = "admin/reviews/";
    private static final String VIEW_INDEX = VIEW_BASE + "index";
    private static final String REDIRECT_REVIEWS = "redirect:" + BASE_PATH;

    private final ReviewService reviewService;
    private final MessageSource messageSource;

    @Autowired
    public ReviewController(ReviewService reviewService, MessageSource messageSource) {
        this.reviewService = reviewService;
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

    @GetMapping("/reviews")
    public String reviewsPage(
        @ModelAttribute AdminPageRequest request,
        Model model
    ) {
        Sort sort = request.getSortDir().equalsIgnoreCase("desc") ?
                Sort.by(request.getSortBy()).descending() : Sort.by(request.getSortBy()).ascending();
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<ReviewResponse> reviews = reviewService.getAllReviews(request.getSearch(), pageable);

        model.addAttribute("reviews", reviews);
        model.addAttribute("currentPage", request.getPage());
        model.addAttribute("totalPages", reviews.getTotalPages());
        model.addAttribute("totalItems", reviews.getTotalElements());
        model.addAttribute("sortBy", request.getSortBy());
        model.addAttribute("sortDir", request.getSortDir());
        model.addAttribute("search", request.getSearch());
        model.addAttribute(AdminConstants.ATTR_ACTIVE_PAGE, AdminConstants.ACTIVE_PAGE_REVIEWS);

        return VIEW_INDEX;
    }

    @PostMapping("/reviews/{id}/approve")
    public String approveReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.toggleReviewStatus(id, ReviewEntity.Status.APPROVED);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_REVIEW_APPROVE_SUCCESS));
        } catch (Exception e) {
            logger.error("Error approving review id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_REVIEW_APPROVE_ERROR, e.getMessage()));
        }
        return REDIRECT_REVIEWS;
    }

    @PostMapping("/reviews/{id}/reject")
    public String rejectReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.toggleReviewStatus(id, ReviewEntity.Status.REJECTED);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_REVIEW_REJECT_SUCCESS));
        } catch (Exception e) {
            logger.error("Error rejecting review id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_REVIEW_REJECT_ERROR, e.getMessage()));
        }
        return REDIRECT_REVIEWS;
    }

    @PostMapping("/reviews/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            addSuccessFlash(redirectAttributes, getMessage(AdminConstants.MSG_REVIEW_DELETE_SUCCESS));
        } catch (Exception e) {
            logger.error("Error deleting review id {}: {}", id, e.getMessage(), e);
            addErrorFlash(redirectAttributes, getMessage(AdminConstants.MSG_REVIEW_DELETE_ERROR, e.getMessage()));
        }
        return REDIRECT_REVIEWS;
    }
}

