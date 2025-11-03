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
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private TourRepository tourRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MessageSource messageSource;
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public Page<BookingResponse> getAllBookings(String search, Pageable pageable) {
        Specification<BookingEntity> spec = createSearchSpecification(search);
        Page<BookingEntity> bookings = bookingRepository.findAll(spec, pageable);
        return bookings.map(this::convertToResponse);
    }

    public BookingResponse getBookingById(Long id) {
        BookingEntity booking = findBookingById(id);
        return convertToResponse(booking);
    }

    public BookingResponse updateBooking(Long id, BookingUpdateRequest request) {
        BookingEntity booking = findBookingById(id);
        Integer oldQty = booking.getQty();
        Integer newQty = request.getQty();
        int qtyDiff = newQty - oldQty;
        
        if (qtyDiff != 0) {
            updateTourSeats(booking.getTour(), qtyDiff);
        }
        
        booking.setQty(newQty);
        booking.setAmount(request.getAmount());
        BookingEntity savedBooking = bookingRepository.save(booking);
        
        return convertToResponse(savedBooking);
    }

    public void deleteBooking(Long id) {
        BookingEntity booking = findBookingById(id);
        bookingRepository.delete(booking);
    }

    public BookingResponse cancelBooking(Long id) {
        BookingEntity booking = findBookingById(id);
        booking.setStatus(BookingEntity.Status.CANCELLED);
        booking.setCancelledAt(java.time.LocalDateTime.now());
        
        BookingEntity savedBooking = bookingRepository.save(booking);
        return convertToResponse(savedBooking);
    }

    public BookingResponse toggleBookingStatus(Long id, BookingEntity.Status newStatus) {
        BookingEntity booking = findBookingById(id);
        
        booking.setStatus(newStatus);
        if (newStatus == BookingEntity.Status.PAID) {
            booking.setPaidAt(java.time.LocalDateTime.now());
        } else if (newStatus == BookingEntity.Status.CANCELLED) {
            booking.setCancelledAt(java.time.LocalDateTime.now());
        } else if (newStatus == BookingEntity.Status.PENDING) {
            booking.setCancelledAt(null);
        }
        
        BookingEntity savedBooking = bookingRepository.save(booking);
        return convertToResponse(savedBooking);
    }
    
    public ApiBookingResponse createBooking(BookingCreateRequest request, Long userId) {
        TourEntity tour = tourRepository.findById(request.getTourId())
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("booking.api.error.tour.not_found", request.getTourId())));
        
        validateTourBooking(tour, request.getQty());
        
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("booking.api.error.user.not_found", userId)));
        
        Double amount = tour.getPrice().multiply(new java.math.BigDecimal(request.getQty())).doubleValue();
        
        BookingEntity booking = BookingEntity.builder()
                .tour(tour)
                .user(user)
                .qty(request.getQty())
                .amount(amount)
                .status(BookingEntity.Status.PENDING)
                .build();
        
        BookingEntity savedBooking = bookingRepository.save(booking);
        updateTourSeats(tour, request.getQty());
        
        return convertToApiResponse(savedBooking);
    }
    
    public Page<ApiBookingResponse> getUserBookings(Long userId, Pageable pageable) {
        Page<BookingEntity> bookings = bookingRepository.findByUserId(userId, pageable);
        return bookings.map(this::convertToApiResponse);
    }
    
    public ApiBookingResponse cancelUserBooking(Long bookingId, Long userId) {
        BookingEntity booking = findBookingById(bookingId);
        
        if (!booking.getUser().getId().equals(userId)) {
            throw new BusinessException(getMessage("booking.api.error.user.own_booking_required"));
        }
        
        if (booking.getStatus() == BookingEntity.Status.CANCELLED) {
            throw new BusinessException(getMessage("booking.api.error.booking.already_cancelled"));
        }
        
        if (booking.getStatus() == BookingEntity.Status.REFUNDED) {
            throw new BusinessException(getMessage("booking.api.error.booking.already_refunded"));
        }
        
        booking.setStatus(BookingEntity.Status.CANCELLED);
        booking.setCancelledAt(java.time.LocalDateTime.now());
        BookingEntity savedBooking = bookingRepository.save(booking);
        
        updateTourSeats(booking.getTour(), -booking.getQty());
        
        return convertToApiResponse(savedBooking);
    }
    
    private ApiBookingResponse convertToApiResponse(BookingEntity booking) {
        return ApiBookingResponse.builder()
                .id(booking.getId())
                .tourId(booking.getTour() != null ? booking.getTour().getId() : null)
                .tourTitle(booking.getTour() != null ? booking.getTour().getTitle() : null)
                .tourLocation(booking.getTour() != null ? booking.getTour().getLocation() : null)
                .qty(booking.getQty())
                .amount(booking.getAmount())
                .status(booking.getStatus())
                .paymentRef(booking.getPaymentRef())
                .paidAt(booking.getPaidAt())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private Specification<BookingEntity> createSearchSpecification(String search) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) {
                return cb.conjunction();
            }
            String searchPattern = "%" + search.toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();
            
            predicates.add(cb.like(cb.lower(root.get("tour").get("title")), searchPattern));
            predicates.add(cb.like(cb.lower(root.get("user").get("name")), searchPattern));
            predicates.add(cb.like(cb.lower(root.get("user").get("email")), searchPattern));
            predicates.add(cb.like(cb.lower(root.get("paymentRef")), searchPattern));
            
            return cb.or(predicates.toArray(new Predicate[0]));
        };
    }

    private BookingResponse convertToResponse(BookingEntity booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .userId(booking.getUser() != null ? booking.getUser().getId() : null)
                .userName(booking.getUser() != null ? booking.getUser().getName() : null)
                .userEmail(booking.getUser() != null ? booking.getUser().getEmail() : null)
                .tourId(booking.getTour() != null ? booking.getTour().getId() : null)
                .tourTitle(booking.getTour() != null ? booking.getTour().getTitle() : null)
                .tourLocation(booking.getTour() != null ? booking.getTour().getLocation() : null)
                .qty(booking.getQty())
                .amount(booking.getAmount())
                .status(booking.getStatus())
                .paymentRef(booking.getPaymentRef())
                .paidAt(booking.getPaidAt())
                .cancelledAt(booking.getCancelledAt())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    private BookingEntity findBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getMessage("booking.api.error.booking.not_found", id)));
    }
    
    private void updateTourSeats(TourEntity tour, int qtyDiff) {
        int newAvailableSeats = tour.getSeatsAvailable() - qtyDiff;
        
        if (newAvailableSeats < 0) {
            throw new BusinessException(getMessage("booking.api.error.booking.cannot_adjust_seats", tour.getSeatsAvailable(), qtyDiff));
        }
        
        tour.setSeatsAvailable(newAvailableSeats);
        tourRepository.save(tour);
    }
    
    private void validateTourBooking(TourEntity tour, Integer requestedQty) {
        if (tour.getStatus() != TourEntity.Status.ACTIVE) {
            throw new BusinessException(getMessage("booking.api.error.tour.not_active"));
        }
        
        if (tour.getSeatsAvailable() < requestedQty) {
            throw new BusinessException(getMessage("booking.api.error.tour.not_enough_seats", tour.getSeatsAvailable(), requestedQty));
        }
    }
}
