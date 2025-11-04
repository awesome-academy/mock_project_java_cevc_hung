package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLikeEntity, Long>, JpaSpecificationExecutor<ReviewLikeEntity> {
    
    Optional<ReviewLikeEntity> findByReviewIdAndUserId(Long reviewId, Long userId);
    
    long countByReviewId(Long reviewId);
    
    boolean existsByReviewIdAndUserId(Long reviewId, Long userId);
}

