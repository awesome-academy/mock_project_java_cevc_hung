package com.mock_project_java_cevc_hung.hunglpmockjava.repository;

import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<TourEntity, Long>, JpaSpecificationExecutor<TourEntity> {
    
    List<TourEntity> findByStatus(TourEntity.Status status);
    
    List<TourEntity> findByCategoryId(Long categoryId);
    
    Page<TourEntity> findByStatus(TourEntity.Status status, Pageable pageable);
    
    @Query("SELECT t FROM TourEntity t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.location) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<TourEntity> findToursWithFilters(@Param("status") TourEntity.Status status, 
                                         @Param("search") String search, 
                                         Pageable pageable);
    
    List<TourEntity> findByStatusAndSeatsAvailableGreaterThan(TourEntity.Status status, Integer seatsAvailable);
    
    long countByStatus(TourEntity.Status status);
    
    // API
    Page<TourEntity> findByCategoryIdAndStatus(Long categoryId, TourEntity.Status status, Pageable pageable);
    
    Page<TourEntity> findByTitleContainingIgnoreCaseAndStatus(String keyword, TourEntity.Status status, Pageable pageable);
    
    Page<TourEntity> findByCategoryIdAndTitleContainingIgnoreCaseAndStatus(Long categoryId, String keyword, TourEntity.Status status, Pageable pageable);
    
    java.util.Optional<TourEntity> findByIdAndStatus(Long id, TourEntity.Status status);
}
