package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.PaymentRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.PaymentResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.messaging.event.PaymentCompletedEvent;
import com.mock_project_java_cevc_hung.hunglpmockjava.messaging.publisher.PaymentEventPublisher;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final MessageSource messageSource;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(
        BookingRepository bookingRepository,
        MessageSource messageSource,
        PaymentEventPublisher paymentEventPublisher
    ) {
        this.bookingRepository = bookingRepository;
        this.messageSource = messageSource;
        this.paymentEventPublisher = paymentEventPublisher;
    }
    
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
        
        LocalDateTime paidAt = LocalDateTime.now();

        paymentEventPublisher.publishPaymentCompleted(PaymentCompletedEvent.builder()
                .bookingId(booking.getId())
                .amount(request.getAmount())
                .paymentRef(request.getPaymentRef())
                .paidAt(paidAt)
                .build());
        
        return PaymentResponse.builder()
            .bookingId(booking.getId())
            .paymentRef(request.getPaymentRef())
            .amount(request.getAmount())
            .status(BookingEntity.Status.PENDING.name())
            .message(getMessage("payment.success"))
            .build();
    }
}

