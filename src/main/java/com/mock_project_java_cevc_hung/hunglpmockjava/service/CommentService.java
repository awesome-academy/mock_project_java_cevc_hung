package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CommentCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.CommentUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiCommentResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CommentEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CommentRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.ReviewRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class CommentService {
    
    @Autowired
    private CommentRepository commentRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    public ApiCommentResponse createComment(CommentCreateRequest request, Long userId) {
        ReviewEntity review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("comment.api.error.review.not_found", request.getReviewId())));
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("comment.api.error.user.not_found", userId)));
        
        CommentEntity comment = CommentEntity.builder()
                .review(review)
                .user(user)
                .content(request.getContent())
                .build();
        
        CommentEntity savedComment = commentRepository.save(comment);
        return convertToResponse(savedComment);
    }
    
    public Page<ApiCommentResponse> getCommentsByReview(Long reviewId, Pageable pageable) {
        Page<CommentEntity> comments = commentRepository.findByReviewId(reviewId, pageable);
        return comments.map(this::convertToResponse);
    }
    
    public ApiCommentResponse getCommentById(Long id) {
        CommentEntity comment = findCommentById(id);
        return convertToResponse(comment);
    }
    
    public ApiCommentResponse updateComment(Long id, Long userId, boolean isAdmin, CommentUpdateRequest request) {
        CommentEntity comment = findCommentById(id);
        
        if (!isAdmin && !comment.getUser().getId().equals(userId)) {
            throw new BusinessException(getMessage("comment.api.error.user.own_comment_required"));
        }
        
        comment.setContent(request.getContent());
        CommentEntity savedComment = commentRepository.save(comment);
        
        return convertToResponse(savedComment);
    }
    
    public void deleteComment(Long id, Long userId, boolean isAdmin) {
        CommentEntity comment = findCommentById(id);
        
        if (!isAdmin && !comment.getUser().getId().equals(userId)) {
            throw new BusinessException(getMessage("comment.api.error.user.own_comment_required"));
        }
        
        commentRepository.delete(comment);
    }
    
    private ApiCommentResponse convertToResponse(CommentEntity comment) {
        return ApiCommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .userName(comment.getUser() != null ? comment.getUser().getName() : null)
                .reviewId(comment.getReview() != null ? comment.getReview().getId() : null)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
    
    private CommentEntity findCommentById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("comment.api.error.comment.not_found", id)));
    }
}

