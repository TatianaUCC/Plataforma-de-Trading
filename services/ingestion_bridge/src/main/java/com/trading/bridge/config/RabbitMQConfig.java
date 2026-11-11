package com.trading.bridge.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la cola 'bridge_ticks_queue' enlazada al exchange 'market_ticks'.
 * El listener opera con acknowledge-mode MANUAL para garantizar que el mensaje
 * RabbitMQ solo se confirme después de que Kafka confirme la recepción.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_MARKET_TICKS  = "market_ticks";
    public static final String QUEUE_BRIDGE_TICKS     = "bridge_ticks_queue";
    public static final String ROUTING_KEY_ALL_TICKS  = "ticks.#";

    @Bean
    public TopicExchange marketTicksExchange() {
        return new TopicExchange(EXCHANGE_MARKET_TICKS, true, false);
    }

    @Bean
    public Queue bridgeTicksQueue() {
        return new Queue(QUEUE_BRIDGE_TICKS, true);
    }

    @Bean
    public Binding bridgeTicksBinding(Queue bridgeTicksQueue, TopicExchange marketTicksExchange) {
        return BindingBuilder
            .bind(bridgeTicksQueue)
            .to(marketTicksExchange)
            .with(ROUTING_KEY_ALL_TICKS);
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

    /**
     * Configura el contenedor de listeners con ACK MANUAL.
     * Esto permite que el listener confirme o reencole el mensaje
     * después de conocer el resultado de la publicación en Kafka.
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        return factory;
    }
}
