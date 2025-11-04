package com.mock_project_java_cevc_hung.hunglpmockjava.controller.api;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.ReviewCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.ReviewUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiPaginatedResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiReviewResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.security.UserDetailsImpl;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        ApiReviewResponse response = reviewService.createReview(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tours/{tourId}/reviews")
    public ResponseEntity<?> getReviewsByTour(
        @PathVariable Long tourId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        
        Long userId = userDetails != null ? userDetails.getId() : null;

        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApiReviewResponse> reviews = reviewService.getReviewsByTour(tourId, userId, pageable);

        ApiPaginatedResponse<ApiReviewResponse> response = ApiPaginatedResponse.<ApiReviewResponse>builder()
                .content(reviews.getContent())
                .page(reviews.getNumber())
                .size(reviews.getSize())
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .first(reviews.isFirst())
                .last(reviews.isLast())
                .hasNext(reviews.hasNext())
                .hasPrevious(reviews.hasPrevious())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<?> getReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails != null ? userDetails.getId() : null;
        ApiReviewResponse response = reviewService.getUserReview(id, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        boolean isAdmin = userDetails.isAdmin();
        ApiReviewResponse response = reviewService.updateUserReview(id, userId, isAdmin, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        boolean isAdmin = userDetails.isAdmin();
        reviewService.deleteUserReview(id, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLikeReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        ApiReviewResponse response = reviewService.toggleLikeReview(id, userId);
        return ResponseEntity.ok(response);
    }
}

