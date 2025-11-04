package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.PaymentRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.PaymentResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.RevenueEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private RevenueService revenueService;
    
    @Autowired
    private MessageSource messageSource;
    
    private String getMessage(String code, Object... args) {
        return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }
    
    /**
     * Process payment for a booking
     * @param request Payment request with booking ID, payment reference, and amount
     * @return PaymentResponse with payment details
     */
    public PaymentResponse processPayment(PaymentRequest request) {
        BookingEntity booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        getMessage("payment.error.booking.not_found", request.getBookingId())));
        
        if (booking.getStatus() != BookingEntity.Status.PENDING) {
            throw new BusinessException(getMessage("payment.error.booking.already_processed", 
                    booking.getStatus().name()));
        }

        double tolerance = 0.01; // 1 cent tolerance
        if (Math.abs(booking.getAmount() - request.getAmount()) > tolerance) {
            throw new BusinessException(getMessage("payment.error.amount.mismatch", 
                    booking.getAmount(), request.getAmount()));
        }
        
        if (request.getPaymentRef() == null || request.getPaymentRef().trim().isEmpty()) {
            throw new BusinessException(getMessage("payment.error.payment_ref.required"));
        }
        
        booking.setStatus(BookingEntity.Status.PAID);
        booking.setPaymentRef(request.getPaymentRef());
        booking.setPaidAt(LocalDateTime.now());
        BookingEntity savedBooking = bookingRepository.save(booking);
        
        LocalDate paymentDate = LocalDate.now();
        RevenueEntity revenue = revenueService.createRevenueForPayment(paymentDate, request.getAmount(), savedBooking);
        
        return PaymentResponse.builder()
            .bookingId(savedBooking.getId())
            .paymentRef(savedBooking.getPaymentRef())
            .amount(savedBooking.getAmount())
            .status(savedBooking.getStatus().name())
            .message(getMessage("payment.success"))
            .revenueId(revenue.getId())
            .build();
    }
}

