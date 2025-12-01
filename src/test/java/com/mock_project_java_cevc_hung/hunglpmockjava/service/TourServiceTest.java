package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.TourResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiTourResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.CategoryEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.CategoryRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TourService Comprehensive Tests")
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TourService tourService;

    private TourEntity testTour;
    private CategoryEntity testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new CategoryEntity();
        testCategory.setName("Adventure");
        testCategory.setId(1L);

        testTour = TourEntity.builder()
                .title("Mountain Trek")
                .description("Amazing mountain adventure")
                .price(new BigDecimal("1500"))
                .location("Himalayas")
                .seatsTotal(100)
                .seatsAvailable(75)
                .status(TourEntity.Status.ACTIVE)
                .category(testCategory)
                .ratingAvg(4.5)
                .build();
        testTour.setId(1L);
    }

    @Test
    @DisplayName("Should get all tours with pagination")
    void getAllTours_WithPageable_ShouldReturnPagedTours() {
        // Given
        Page<TourEntity> page = new PageImpl<>(List.of(testTour));
        Pageable pageable = PageRequest.of(0, 10);
        when(tourRepository.findAll(pageable)).thenReturn(page);

        // When
        Page<TourResponse> result = tourService.getAllTours(pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Mountain Trek");
    }

    @Test
    @DisplayName("Should get tour by id")
    void getTourById_ExistingTour_ShouldReturnTour() {
        // Given
        when(tourRepository.findById(anyLong())).thenReturn(Optional.of(testTour));

        // When
        TourResponse result = tourService.getTourById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Mountain Trek");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    @DisplayName("Should throw exception when tour not found")
    void getTourById_NonExistingTour_ShouldThrowException() {
        // Given
        when(tourRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tourService.getTourById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Tour not found");
    }

    @Test
    @DisplayName("Should get tours by status")
    void getToursByStatus_ValidStatus_ShouldReturnTours() {
        // Given
        when(tourRepository.findByStatus(any())).thenReturn(List.of(testTour));

        // When
        List<TourResponse> result = tourService.getToursByStatus(TourEntity.Status.ACTIVE);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result).hasSize(1);
        // Status in TourResponse is String
        assertThat(result.get(0).getStatus()).asString().contains("ACTIVE");
    }

    @Test
    @DisplayName("Should get public tours with filters")
    void getPublicTours_WithCategoryAndKeyword_ShouldReturnFilteredTours() {
        // Given
        Page<TourEntity> page = new PageImpl<>(List.of(testTour));
        Pageable pageable = PageRequest.of(0, 10);
        when(tourRepository.findByCategoryIdAndStatus(anyLong(), eq(TourEntity.Status.ACTIVE), any()))
                .thenReturn(page);

        // When
        Page<ApiTourResponse> result = tourService.getPublicTours(1L, null, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        verify(tourRepository, times(1)).findByCategoryIdAndStatus(anyLong(), eq(TourEntity.Status.ACTIVE), any());
    }

    @Test
    @DisplayName("Should get public tour by id for active tours only")
    void getPublicTourById_ActiveTour_ShouldReturnTour() {
        // Given
        when(tourRepository.findByIdAndStatus(anyLong(), eq(TourEntity.Status.ACTIVE)))
                .thenReturn(Optional.of(testTour));

        // When
        ApiTourResponse result = tourService.getPublicTourById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Mountain Trek");
    }

    @Test
    @DisplayName("Should toggle tour status from ACTIVE to INACTIVE")
    void toggleTourStatus_ActiveTour_ShouldChangeToInactive() {
        // Given
        testTour.setStatus(TourEntity.Status.ACTIVE);
        when(tourRepository.findById(anyLong())).thenReturn(Optional.of(testTour));
        when(tourRepository.save(any(TourEntity.class))).thenReturn(testTour);

        // When
        TourResponse result = tourService.toggleTourStatus(1L);

        // Then
        assertThat(testTour.getStatus()).isEqualTo(TourEntity.Status.INACTIVE);
        verify(tourRepository, times(1)).save(testTour);
    }

    @Test
    @DisplayName("Should toggle tour status from INACTIVE to ACTIVE")
    void toggleTourStatus_InactiveTour_ShouldChangeToActive() {
        // Given
        testTour.setStatus(TourEntity.Status.INACTIVE);
        when(tourRepository.findById(anyLong())).thenReturn(Optional.of(testTour));
        when(tourRepository.save(any(TourEntity.class))).thenReturn(testTour);

        // When
        TourResponse result = tourService.toggleTourStatus(1L);

        // Then
        assertThat(testTour.getStatus()).isEqualTo(TourEntity.Status.ACTIVE);
        verify(tourRepository, times(1)).save(testTour);
    }

    @Test
    @DisplayName("Should delete tour successfully")
    void deleteTour_ExistingTour_ShouldDeleteTour() {
        // Given
        when(tourRepository.findById(anyLong())).thenReturn(Optional.of(testTour));
        doNothing().when(tourRepository).delete(any(TourEntity.class));

        // When
        tourService.deleteTour(1L);

        // Then
        verify(tourRepository, times(1)).delete(testTour);
    }

    @Test
    @DisplayName("Should get tours with filters")
    void getToursWithFilters_WithStatusAndSearch_ShouldReturnFilteredTours() {
        // Given
        Page<TourEntity> page = new PageImpl<>(List.of(testTour));
        Pageable pageable = PageRequest.of(0, 10);
        when(tourRepository.findToursWithFilters(any(), anyString(), eq(pageable)))
                .thenReturn(page);

        // When
        Page<TourResponse> result = tourService.getToursWithFilters(TourEntity.Status.ACTIVE, "Mountain", pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
    }
}
