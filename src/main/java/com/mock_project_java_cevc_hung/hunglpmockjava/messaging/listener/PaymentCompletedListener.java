package com.mock_project_java_cevc_hung.hunglpmockjava.messaging.listener;

import com.mock_project_java_cevc_hung.hunglpmockjava.config.RabbitMQConfig;
import com.mock_project_java_cevc_hung.hunglpmockjava.entity.BookingEntity;
import com.mock_project_java_cevc_hung.hunglpmockjava.messaging.event.PaymentCompletedEvent;
import com.mock_project_java_cevc_hung.hunglpmockjava.messaging.exception.PaymentProcessingException;
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

/**
 * Listens for {@code payment.completed} events published after synchronous payment acceptance.
 * The listener validates the payload, persists the booking state transition to {@link BookingEntity.Status#PAID},
 * and records the corresponding revenue entry. All heavy lifting happens asynchronously so the HTTP payment call
 * can respond quickly. The handler is idempotent: duplicate events for already-paid bookings are detected and skipped.
 */
@Component
public class PaymentCompletedListener {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCompletedListener.class);
    /**
     * Acceptable delta (one cent) when comparing booking amount vs incoming payload. This reflects
     * the minimum currency precision used across the system (VND currently stored as double). Any wider gap
     * indicates a data inconsistency and triggers a retry/DLQ.
     */
    private static final double AMOUNT_TOLERANCE = 0.01d;

    private final BookingRepository bookingRepository;
    private final RevenueService revenueService;

    public PaymentCompletedListener(BookingRepository bookingRepository, RevenueService revenueService) {
        this.bookingRepository = bookingRepository;
        this.revenueService = revenueService;
    }

    /**
     * Consume payment completion events, validate booking state, persist payment info and revenue.
     * <p>Validation steps:</p>
     * <ul>
     *   <li>Booking must exist</li>
     *   <li>Duplicate events for already-paid bookings are ignored (idempotency)</li>
     *   <li>Amount must match within {@link #AMOUNT_TOLERANCE}</li>
     * </ul>
     * <p>If validation fails, a {@link PaymentProcessingException} propagates, letting RabbitMQ retry and eventually
     * route the message to the DLQ. The method runs inside a transaction to ensure booking updates and revenue
     * creation are atomic.</p>
     */
    @RabbitListener(queues = RabbitMQConfig.PAYMENT_COMPLETED_QUEUE,
            containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        logger.info("Received payment.completed event bookingId={}, amount={}",
                event.getBookingId(), event.getAmount());

        Optional<BookingEntity> optionalBooking = bookingRepository.findById(event.getBookingId());
        if (optionalBooking.isEmpty()) {
            throw new PaymentProcessingException(
                    "Booking not found for payment event bookingId=" + event.getBookingId());
        }

        BookingEntity booking = optionalBooking.get();
        if (booking.getStatus() == BookingEntity.Status.PAID) {
            logger.info("Booking {} already paid. Skipping.", booking.getId());
            return;
        }

        if (Math.abs(booking.getAmount() - event.getAmount()) > AMOUNT_TOLERANCE) {
            throw new PaymentProcessingException(String.format(
                    "Amount mismatch with booking %d. Expected %.2f, received %.2f",
                    booking.getId(), booking.getAmount(), event.getAmount()));
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

