package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.ReviewCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.ReviewUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.ReviewResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiReviewResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.ReviewLikeRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.ReviewRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.TourRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private TourRepository tourRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ReviewLikeRepository reviewLikeRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public Page<ReviewResponse> getAllReviews(String search, Pageable pageable) {
        Specification<ReviewEntity> spec = createSearchSpecification(search);
        Page<ReviewEntity> reviews = reviewRepository.findAll(spec, pageable);
        return reviews.map(this::convertToResponse);
    }

    public ReviewResponse getReviewById(Long id) {
        ReviewEntity review = findReviewById(id);
        return convertToResponse(review);
    }

    public ReviewResponse updateReview(Long id, ReviewUpdateRequest request) {
        ReviewEntity review = findReviewById(id);
        
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        
        ReviewEntity savedReview = reviewRepository.save(review);
        return convertToResponse(savedReview);
    }

    public void deleteReview(Long id) {
        ReviewEntity review = findReviewById(id);
        reviewRepository.delete(review);
    }

    public ReviewResponse toggleReviewStatus(Long id, ReviewEntity.Status newStatus) {
        ReviewEntity review = findReviewById(id);
        
        review.setStatus(newStatus);
        
        // Update tour rating when review status changes to APPROVED or REJECTED
        if (newStatus == ReviewEntity.Status.APPROVED || newStatus == ReviewEntity.Status.REJECTED) {
            updateTourRating(review.getTour().getId());
        }
        
        ReviewEntity savedReview = reviewRepository.save(review);
        return convertToResponse(savedReview);
    }
    
    public ApiReviewResponse createReview(ReviewCreateRequest request, Long userId) {
        TourEntity tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("review.api.error.tour.not_found", request.getTourId())));
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("review.api.error.user.not_found", userId)));
        
        if (reviewRepository.existsByTourIdAndUserId(request.getTourId(), userId)) {
            throw new BusinessException(getMessage("review.api.error.already_reviewed"));
        }
        
        ReviewEntity review = ReviewEntity.builder()
                .tour(tour)
                .user(user)
                .rating(request.getRating())
                .content(request.getContent())
                .type(request.getType())
                .status(ReviewEntity.Status.PENDING)
                .build();
        
        ReviewEntity savedReview = reviewRepository.save(review);
        
        return convertToApiResponse(savedReview, userId);
    }
    
    public Page<ApiReviewResponse> getReviewsByTour(Long tourId, Long userId, Pageable pageable) {
        Page<ReviewEntity> reviews = reviewRepository.findByTourIdAndStatus(tourId, ReviewEntity.Status.APPROVED, pageable);
        return reviews.map(review -> convertToApiResponse(review, userId));
    }
    
    public ApiReviewResponse getUserReview(Long reviewId, Long userId) {
        ReviewEntity review = findReviewById(reviewId);
        return convertToApiResponse(review, userId);
    }
    
    public ApiReviewResponse updateUserReview(Long reviewId, Long userId, boolean isAdmin, ReviewUpdateRequest request) {
        ReviewEntity review = findReviewById(reviewId);
        
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new BusinessException(getMessage("review.api.error.user.own_review_required"));
        }
        
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        
        ReviewEntity savedReview = reviewRepository.save(review);
        
        if (savedReview.getStatus() == ReviewEntity.Status.APPROVED) {
            updateTourRating(review.getTour().getId());
        }
        
        return convertToApiResponse(savedReview, userId);
    }
    
    public void deleteUserReview(Long reviewId, Long userId, boolean isAdmin) {
        ReviewEntity review = findReviewById(reviewId);
        
        if (!isAdmin && !review.getUser().getId().equals(userId)) {
            throw new BusinessException(getMessage("review.api.error.user.own_review_required"));
        }
        
        Long tourId = review.getTour().getId();
        reviewRepository.delete(review);
        
        updateTourRating(tourId);
    }
    
    public ApiReviewResponse toggleLikeReview(Long reviewId, Long userId) {
        ReviewEntity review = findReviewById(reviewId);
        
        boolean isLiked = reviewLikeRepository.existsByReviewIdAndUserId(reviewId, userId);
        
        if (isLiked) {
            reviewLikeRepository.findByReviewIdAndUserId(reviewId, userId)
                    .ifPresent(like -> reviewLikeRepository.delete(like));
        } else {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(getMessage("review.api.error.user.not_found", userId)));
            
            com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewLikeEntity like = 
                    com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewLikeEntity.builder()
                            .review(review)
                            .user(user)
                            .build();
            
            reviewLikeRepository.save(like);
        }
        
        return convertToApiResponse(review, userId);
    }

    private Specification<ReviewEntity> createSearchSpecification(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.like(cb.lower(root.get("content")), searchPattern));
            predicates.add(cb.like(cb.lower(root.get("tour").get("title")), searchPattern));
            predicates.add(cb.like(cb.lower(root.get("user").get("name")), searchPattern));
            predicates.add(cb.like(cb.lower(root.get("user").get("email")), searchPattern));
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private ReviewResponse convertToResponse(ReviewEntity review) {
        long totalLikes = reviewLikeRepository.countByReviewId(review.getId());
        
        return ReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getName() : null)
                .userEmail(review.getUser() != null ? review.getUser().getEmail() : null)
                .tourId(review.getTour() != null ? review.getTour().getId() : null)
                .tourTitle(review.getTour() != null ? review.getTour().getTitle() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .status(review.getStatus())
                .type(review.getType())
                .totalLikes(totalLikes)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    private ApiReviewResponse convertToApiResponse(ReviewEntity review, Long userId) {
        long totalLikes = reviewLikeRepository.countByReviewId(review.getId());
        boolean isLiked = userId != null && reviewLikeRepository.existsByReviewIdAndUserId(review.getId(), userId);
        
        return ApiReviewResponse.builder()
                .id(review.getId())
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .userName(review.getUser() != null ? review.getUser().getName() : null)
                .tourId(review.getTour() != null ? review.getTour().getId() : null)
                .tourTitle(review.getTour() != null ? review.getTour().getTitle() : null)
                .rating(review.getRating())
                .content(review.getContent())
                .type(review.getType())
                .totalLikes(totalLikes)
                .isLiked(isLiked)
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    private ReviewEntity findReviewById(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("review.api.error.review.not_found", id)));
    }
    
    private void updateTourRating(Long tourId) {
        TourEntity tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("review.api.error.tour.not_found", tourId)));
        
        Double avgRating = reviewRepository.calculateAverageRating(tourId, ReviewEntity.Status.APPROVED);
        tour.setRatingAvg(avgRating != null ? avgRating : 0.0);
        
        tourRepository.save(tour);
    }
    
}

