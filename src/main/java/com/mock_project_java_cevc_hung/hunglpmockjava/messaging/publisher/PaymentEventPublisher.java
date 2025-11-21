package com.mock_project_java_cevc_hung.hunglpmockjava.messaging.publisher;

import com.mock_project_java_cevc_hung.hunglpmockjava.config.RabbitMQConfig;
import com.mock_project_java_cevc_hung.hunglpmockjava.messaging.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PaymentEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_COMPLETED_ROUTING_KEY,
                event
        );
        logger.info("Published payment.completed event for bookingId={}", event.getBookingId());
    }
}

