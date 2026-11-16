package com.trading.consoleworker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la cola 'console_alert_queue' ligada al Fanout Exchange 'console_alerts'.
 *
 * El Fanout Exchange garantiza que TODOS los console_alert_worker reciban
 * cada mensaje — a diferencia del email_queue donde solo UNO lo procesa.
 *
 * La cola es durable para sobrevivir reinicios del broker.
 * El listener usa ACK manual para garantizar el procesamiento.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_CONSOLE_ALERTS = "console_alerts";
    public static final String QUEUE_CONSOLE_ALERTS    = "console_alert_queue";

    @Bean
    public FanoutExchange consoleAlertsExchange() {
        return new FanoutExchange(EXCHANGE_CONSOLE_ALERTS, true, false);
    }

    @Bean
    public Queue consoleAlertQueue() {
        return new Queue(QUEUE_CONSOLE_ALERTS, true);
    }

    @Bean
    public Binding consoleAlertBinding(Queue consoleAlertQueue,
                                       FanoutExchange consoleAlertsExchange) {
        return BindingBuilder.bind(consoleAlertQueue).to(consoleAlertsExchange);
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
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        return factory;
    }
}
