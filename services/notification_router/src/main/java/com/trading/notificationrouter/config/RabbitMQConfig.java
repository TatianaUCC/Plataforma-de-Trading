package com.trading.notificationrouter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura los destinos RabbitMQ del notification_router:
 *   - email_queue (Work Queue durable)
 *   - console_alerts (Fanout Exchange durable)
 *
 * Los email_worker y console_alert_worker declaran sus propias colas/bindings.
 * El notification_router solo declara lo que necesita para publicar.
 */
@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_EMAIL            = "email_queue";
    public static final String EXCHANGE_CONSOLE_ALERTS = "console_alerts";

    @Bean
    public Queue emailQueue() {
        return new Queue(QUEUE_EMAIL, true);
    }

    @Bean
    public FanoutExchange consoleAlertsExchange() {
        return new FanoutExchange(EXCHANGE_CONSOLE_ALERTS, true, false);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
