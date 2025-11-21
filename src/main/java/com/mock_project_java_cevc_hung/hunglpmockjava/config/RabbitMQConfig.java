package com.mock_project_java_cevc_hung.hunglpmockjava.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_DLX = "payment.deadletter.exchange";

    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";
    public static final String PAYMENT_COMPLETED_ROUTING_KEY = "payment.completed";
    public static final String PAYMENT_COMPLETED_DLQ = "payment.completed.dlq";
    public static final String PAYMENT_COMPLETED_DLQ_ROUTING_KEY = "payment.completed.dlq";

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange paymentDeadLetterExchange() {
        return new TopicExchange(PAYMENT_DLX, true, false);
    }

    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", PAYMENT_DLX)
                .withArgument("x-dead-letter-routing-key", PAYMENT_COMPLETED_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue paymentCompletedDeadLetterQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_DLQ).build();
    }

    @Bean
    public Binding paymentCompletedBinding(Queue paymentCompletedQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentCompletedQueue)
                .to(paymentExchange)
                .with(PAYMENT_COMPLETED_ROUTING_KEY);
    }

    @Bean
    public Binding paymentCompletedDlqBinding(Queue paymentCompletedDeadLetterQueue,
                                              TopicExchange paymentDeadLetterExchange) {
        return BindingBuilder.bind(paymentCompletedDeadLetterQueue)
                .to(paymentDeadLetterExchange)
                .with(PAYMENT_COMPLETED_DLQ_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean(name = "rabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(false);
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 10000)
                        .recoverer(new RejectAndDontRequeueRecoverer())
                        .build()
        );
        factory.setErrorHandler(throwable ->
                logger.error("Rabbit listener error. Message will be routed to DLQ if retries exhausted", throwable));
        return factory;
    }
}

