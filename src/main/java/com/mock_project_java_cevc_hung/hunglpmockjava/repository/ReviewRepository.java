package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.ReviewEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.projection.TopRatedTourProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long>, JpaSpecificationExecutor<ReviewEntity> {
    
    Page<ReviewEntity> findByTourIdAndStatus(Long tourId, ReviewEntity.Status status, Pageable pageable);
    
    List<ReviewEntity> findByTourIdAndStatus(Long tourId, ReviewEntity.Status status);
    
    @Query("SELECT AVG(r.rating) FROM ReviewEntity r WHERE r.tour.id = :tourId AND r.status = :status")
    Double calculateAverageRating(@Param("tourId") Long tourId, @Param("status") ReviewEntity.Status status);
    
    long countByTourIdAndStatus(Long tourId, ReviewEntity.Status status);
    
    @Query("SELECT COUNT(r) > 0 FROM ReviewEntity r WHERE r.tour.id = :tourId AND r.user.id = :userId")
    boolean existsByTourIdAndUserId(@Param("tourId") Long tourId, @Param("userId") Long userId);

    @Query(
            value = """
                    SELECT t.id AS tourId,
                           t.title AS tourTitle,
                           COALESCE(AVG(r.rating), 0) AS avgRating,
                           COUNT(r.id) AS totalReviews
                    FROM reviews r
                    JOIN tours t ON r.tour_id = t.id
                    WHERE r.status = :status
                    GROUP BY t.id, t.title
                    HAVING COUNT(r.id) > 0
                    ORDER BY avgRating DESC, totalReviews DESC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<TopRatedTourProjection> findTopRatedTours(@Param("status") String status, @Param("limit") int limit);
}
