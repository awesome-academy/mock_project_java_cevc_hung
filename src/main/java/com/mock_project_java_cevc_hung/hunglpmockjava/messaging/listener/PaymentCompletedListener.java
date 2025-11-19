package com.mock_project_java_cevc_hung.hunglpmockjava.messaging.listener;

import com.mock_project_java_cevc_hung.hunglpmockjava.config.RabbitMQConfig;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.messaging.event.PaymentCompletedEvent;
import com.mock_project_java_cevc_hung.hunglpmockjava.repository.BookingRepository;
import com.mock_project_java_cevc_hung.hunglpmockjava.service.RevenueService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class PaymentCompletedListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCompletedListener.class);
    private static final double AMOUNT_TOLERANCE = 0.01d;

    private final BookingRepository bookingRepository;
    private final RevenueService revenueService;

    public PaymentCompletedListener(BookingRepository bookingRepository, RevenueService revenueService) {
        this.bookingRepository = bookingRepository;
        this.revenueService = revenueService;
    }

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE)
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        logger.info("Received payment.completed event bookingId={}, amount={}",
                event.getBookingId(), event.getAmount());

        Optional<BookingEntity> optionalBooking = bookingRepository.findById(event.getBookingId());
        if (optionalBooking.isEmpty()) {
            logger.error("Booking not found for payment event bookingId={}", event.getBookingId());
            return;
        }

        BookingEntity booking = optionalBooking.get();
        if (booking.getStatus() == BookingEntity.Status.PAID) {
            logger.info("Booking {} already paid. Skipping.", booking.getId());
            return;
        }

        if (Math.abs(booking.getAmount() - event.getAmount()) > AMOUNT_TOLERANCE) {
            logger.error("Amount unmatch with booking {}. Expected {}, receiveunmatchd {}",
                    booking.getId(), booking.getAmount(), event.getAmount());
            return;
        }

        booking.setStatus(BookingEntity.Status.PAID);
        booking.setPaymentRef(event.getPaymentRef());
        booking.setPaidAt(event.getPaidAt() != null ? event.getPaidAt() : LocalDateTime.now());

        BookingEntity savedBooking = bookingRepository.save(booking);

        revenueService.createRevenueForPayment(
                savedBooking.getPaidAt() != null ? savedBooking.getPaidAt().toLocalDate() : LocalDate.now(),
                savedBooking.getAmount(),
                savedBooking
        );

        logger.info("Payment event processed successfully for booking {}", booking.getId());
    }
}

