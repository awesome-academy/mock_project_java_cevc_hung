package com.mock_project_java_cevc_hung.hunglpmockjava.controller.api;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CommentCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CommentUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiCommentResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiPaginatedResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.security.UserDetailsImpl;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.CommentService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ApiCommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createComment(
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        ApiCommentResponse response = commentService.createComment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reviews/{reviewId}/comments")
    public ResponseEntity<?> getCommentsByReview(
            @PathVariable Long reviewId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100;

        Sort sort = sortDir.equalsIgnoreCase("desc") ?
                   Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ApiCommentResponse> comments = commentService.getCommentsByReview(reviewId, pageable);

        ApiPaginatedResponse<ApiCommentResponse> response = ApiPaginatedResponse.<ApiCommentResponse>builder()
                .content(comments.getContent())
                .page(comments.getNumber())
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .first(comments.isFirst())
                .last(comments.isLast())
                .hasNext(comments.hasNext())
                .hasPrevious(comments.hasPrevious())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<?> getComment(@PathVariable Long id) {
        ApiCommentResponse response = commentService.getCommentById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        boolean isAdmin = userDetails.isAdmin();
        
        ApiCommentResponse existingComment = commentService.getCommentById(id);
        if (!isAdmin && !existingComment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", "You can only modify your own comments"));
        }
        
        ApiCommentResponse response = commentService.updateComment(id, userId, isAdmin, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        
        Long userId = userDetails.getId();
        boolean isAdmin = userDetails.isAdmin();
        
        ApiCommentResponse existingComment = commentService.getCommentById(id);
        if (!isAdmin && !existingComment.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Forbidden", "message", "You can only delete your own comments"));
        }
        
        commentService.deleteComment(id, userId, isAdmin);
        return ResponseEntity.noContent().build();
    }
}

