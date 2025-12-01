package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.BookingCreateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.BookingUpdateRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.BookingResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.api.ApiBookingResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.TourEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.UserEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.BookingRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.TourRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Comprehensive Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TourRepository tourRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private BookingService bookingService;

    private TourEntity testTour;
    private UserEntity testUser;
    private BookingEntity testBooking;
    private BookingCreateRequest bookingRequest;

    @BeforeEach
    void setUp() {
        testUser = UserEntity.builder()
                .email("user@example.com")
                .name("Test User")
                .build();
        testUser.setId(1L);

        testTour = TourEntity.builder()
                .title("Test Tour")
                .price(new BigDecimal("1000"))
                .seatsTotal(100)
                .seatsAvailable(50)
                .status(TourEntity.Status.ACTIVE)
                .build();
        testTour.setId(1L);

        testBooking = BookingEntity.builder()
                .tour(testTour)
                .user(testUser)
                .qty(2)
                .amount(2000.0)
                .status(BookingEntity.Status.PENDING)
                .build();
        testBooking.setId(1L);

        bookingRequest = BookingCreateRequest.builder()
                .tourId(1L)
                .qty(2)
                .amount(2000.0)
                .build();

        lenient().when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Message");
    }

    @Test
    @DisplayName("Should create booking successfully")
    void createBooking_ValidRequest_ShouldCreateBooking() {
        // Given
        when(tourRepository.findById(anyLong())).thenReturn(Optional.of(testTour));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(testBooking);
        when(tourRepository.save(any(TourEntity.class))).thenReturn(testTour);

        // When
        ApiBookingResponse result = bookingService.createBooking(bookingRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQty()).isEqualTo(2);
        assertThat(testTour.getSeatsAvailable()).isEqualTo(48); // 50 - 2
        verify(bookingRepository, times(1)).save(any(BookingEntity.class));
        verify(tourRepository, times(1)).save(testTour);
    }

    @Test
    @DisplayName("Should throw exception when tour not found")
    void createBooking_NonExistingTour_ShouldThrowException() {
        // Given
        when(tourRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void createBooking_NonExistingUser_ShouldThrowException() {
        // Given
        when(tourRepository.findById(anyLong())).thenReturn(Optional.of(testTour));
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, 1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when not enough seats available")
    void createBooking_NotEnoughSeats_ShouldThrowException() {
        // Given
        testTour.setSeatsAvailable(1);
        bookingRequest.setQty(5);
        when(tourRepository.findById(anyLong())).thenReturn(Optional.of(testTour));

        // When & Then
        assertThatThrownBy(() -> bookingService.createBooking(bookingRequest, 1L))
                .isInstanceOf(BusinessException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get user bookings with pagination")
    void getUserBookings_ValidUserId_ShouldReturnBookings() {
        // Given
        Page<BookingEntity> page = new PageImpl<>(List.of(testBooking));
        Pageable pageable = PageRequest.of(0, 10);
        when(bookingRepository.findByUserId(anyLong(), any())).thenReturn(page);

        // When
        Page<ApiBookingResponse> result = bookingService.getUserBookings(1L, pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getQty()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should get booking by id")
    void getBookingById_ExistingBooking_ShouldReturnBooking() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        // When
        BookingResponse result = bookingService.getBookingById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQty()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should cancel user booking and restore seats")
    void cancelUserBooking_ValidBooking_ShouldCancelAndRestoreSeats() {
        // Given
        testBooking.setTour(testTour);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(testBooking);
        when(tourRepository.save(any(TourEntity.class))).thenReturn(testTour);

        // When
        bookingService.cancelUserBooking(1L, 1L);

        // Then
        assertThat(testBooking.getStatus()).isEqualTo(BookingEntity.Status.CANCELLED);
        assertThat(testBooking.getCancelledAt()).isNotNull();
        assertThat(testTour.getSeatsAvailable()).isEqualTo(52); // 50 + 2
        verify(bookingRepository, times(1)).save(testBooking);
        verify(tourRepository, times(1)).save(testTour);
    }

    @Test
    @DisplayName("Should throw exception when non-owner tries to cancel")
    void cancelUserBooking_NonOwner_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        // When & Then
        assertThatThrownBy(() -> bookingService.cancelUserBooking(1L, 999L))
                .isInstanceOf(BusinessException.class);

        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not restore seats when cancelling already cancelled booking")
    void cancelBooking_AlreadyCancelled_ShouldNotRestoreSeats() {
        // Given
        testBooking.setStatus(BookingEntity.Status.CANCELLED);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any())).thenReturn(testBooking);

        // When
        bookingService.cancelBooking(1L);

        // Then
        verify(tourRepository, never()).findById(anyLong());
        verify(tourRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update booking status successfully")
    void toggleBookingStatus_ValidBooking_ShouldUpdateStatus() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(testBooking);

        // When
        BookingResponse result = bookingService.toggleBookingStatus(1L, BookingEntity.Status.PAID);

        // Then
        assertThat(result).isNotNull();
        assertThat(testBooking.getStatus()).isEqualTo(BookingEntity.Status.PAID);
        verify(bookingRepository, times(1)).save(testBooking);
    }

    @Test
    @DisplayName("Should get all bookings with search")
    void getAllBookings_WithSearch_ShouldReturnFilteredBookings() {
        // Given
        Page<BookingEntity> page = new PageImpl<>(List.of(testBooking));
        Pageable pageable = PageRequest.of(0, 10);
        when(bookingRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                .thenReturn(page);

        // When
        Page<BookingResponse> result = bookingService.getAllBookings("Test", pageable);

        // Then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
    }
}
