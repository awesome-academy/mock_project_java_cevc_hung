package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.TourCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.TourUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TourResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CategoryEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.TourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TourService {

    @Autowired
    private TourRepository tourRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<TourResponse> getAllTours(Pageable pageable) {
        Page<TourEntity> tours = tourRepository.findAll(pageable);
        return tours.map(this::convertToResponse);
    }

    public Page<TourResponse> getToursWithFilters(TourEntity.Status status, String search, Pageable pageable) {
        Page<TourEntity> tours = tourRepository.findToursWithFilters(status, search, pageable);
        return tours.map(this::convertToResponse);
    }

    public TourResponse getTourById(Long id) {
        TourEntity tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + id));
        return convertToResponse(tour);
    }

    public TourResponse createTour(TourCreateRequest request) {
        // Validate category exists
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        TourEntity tour = TourEntity.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .location(request.getLocation())
                .thumbnailUrl("https://via.placeholder.com/300x200?text=Tour+Image")
                .seatsTotal(request.getSeatsTotal())
                .seatsAvailable(request.getSeatsAvailable() != null ? request.getSeatsAvailable() : request.getSeatsTotal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .category(category)
                .status(TourEntity.Status.ACTIVE)
                .build();

        TourEntity savedTour = tourRepository.save(tour);
        return convertToResponse(savedTour);
    }

    public TourResponse updateTour(Long id, TourUpdateRequest request) {
        TourEntity tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + id));

        // Update fields
        tour.setTitle(request.getTitle());
        tour.setDescription(request.getDescription());
        tour.setPrice(request.getPrice());
        tour.setLocation(request.getLocation());
        tour.setSeatsTotal(request.getSeatsTotal());
        tour.setSeatsAvailable(request.getSeatsAvailable());
        tour.setStartDate(request.getStartDate());
        tour.setEndDate(request.getEndDate());

        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            tour.setCategory(category);
        }

        TourEntity savedTour = tourRepository.save(tour);
        return convertToResponse(savedTour);
    }

    public void deleteTour(Long id) {
        TourEntity tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + id));
        tourRepository.delete(tour);
    }

    public TourResponse toggleTourStatus(Long id) {
        TourEntity tour = tourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tour not found with id: " + id));
        
        tour.setStatus(tour.getStatus() == TourEntity.Status.ACTIVE ? 
                      TourEntity.Status.INACTIVE : TourEntity.Status.ACTIVE);
        
        TourEntity savedTour = tourRepository.save(tour);
        return convertToResponse(savedTour);
    }

    public List<TourResponse> getToursByStatus(TourEntity.Status status) {
        List<TourEntity> tours = tourRepository.findByStatus(status);
        return tours.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private TourResponse convertToResponse(TourEntity tour) {
        return TourResponse.builder()
                .id(tour.getId())
                .title(tour.getTitle())
                .description(tour.getDescription())
                .price(tour.getPrice())
                .location(tour.getLocation())
                .thumbnailUrl(tour.getThumbnailUrl())
                .seatsTotal(tour.getSeatsTotal())
                .seatsAvailable(tour.getSeatsAvailable())
                .ratingAvg(tour.getRatingAvg())
                .startDate(tour.getStartDate())
                .endDate(tour.getEndDate())
                .status(tour.getStatus())
                .categoryName(tour.getCategory() != null ? tour.getCategory().getName() : null)
                .categoryId(tour.getCategory() != null ? tour.getCategory().getId() : null)
                .createdAt(tour.getCreatedAt())
                .updatedAt(tour.getUpdatedAt())
                .totalBookings(tour.getBookings() != null ? (long) tour.getBookings().size() : 0L)
                .totalReviews(tour.getReviews() != null ? (long) tour.getReviews().size() : 0L)
                .build();
    }
}
