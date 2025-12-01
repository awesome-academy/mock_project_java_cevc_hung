package com.mock_project_java_cevc_hung.hunglpmockjava.service;

import com.mock_project_java_cevc_hung.hunglpmockjava.dto.request.PaymentRequest;
import com.mock_project_java_cevc_hung.hunglpmockjava.dto.response.PaymentResponse;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.BusinessException;
import com.mock_project_java_cevc_hung.hunglpmockjava.exception.ResourceNotFoundException;
import com.mock_project_java_cevc_hung.hunglpmockjava.messaging.publisher.PaymentEventPublisher;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private BookingEntity testBooking;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        testBooking = BookingEntity.builder()
                .amount(2000.0)
                .status(BookingEntity.Status.PENDING)
                .build();
        // Set ID manually since @GeneratedValue won't work in tests
        testBooking.setId(1L);

        paymentRequest = PaymentRequest.builder()
                .bookingId(1L)
                .paymentRef("PAY-123456")
                .amount(2000.0)
                .build();

        lenient().when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Message");
    }

    @Test
    @DisplayName(" Should process payment successfully")
    void processPayment_ValidRequest_ShouldCompletePayment() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));
        doNothing().when(paymentEventPublisher).publishPaymentCompleted(any());

        // When
        PaymentResponse result = paymentService.processPayment(paymentRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getBookingId()).isEqualTo(1L);
        assertThat(result.getPaymentRef()).isEqualTo("PAY-123456");
        assertThat(result.getAmount()).isEqualTo(2000.0);
        verify(paymentEventPublisher, times(1)).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Should throw exception when booking not found")
    void processPayment_NonExistingBooking_ShouldThrowException() {
        // Given
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(paymentEventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Should throw exception when booking already processed")
    void processPayment_AlreadyProcessed_ShouldThrowException() {
        // Given
        testBooking.setStatus(BookingEntity.Status.PAID);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        // When & Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(BusinessException.class);

        verify(paymentEventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Should throw exception when payment amount doesn't match")
    void processPayment_AmountMismatch_ShouldThrowException() {
        // Given
        paymentRequest.setAmount(1500.0); // Different from booking
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        // When & Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(BusinessException.class);

        verify(paymentEventPublisher, never()).publishPaymentCompleted(any());
    }

    @Test
    @DisplayName("Should throw exception when payment ref is empty")
    void processPayment_EmptyPaymentRef_ShouldThrowException() {
        // Given
        paymentRequest.setPaymentRef("");
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(testBooking));

        // When & Then
        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest))
                .isInstanceOf(BusinessException.class);

        verify(paymentEventPublisher, never()).publishPaymentCompleted(any());
    }
}
