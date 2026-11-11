package com.trading.livedashboard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la cola 'live_ticks_queue' y la enlaza al exchange 'market_ticks'
 * con routing key 'ticks.#' para recibir todos los activos.
 *
 * El exchange ya fue declarado por market_data_feed. Se declara aquí también
 * porque Spring AMQP lo hace idempotente: si ya existe con los mismos parámetros,
 * no produce error.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_MARKET_TICKS = "market_ticks";
    public static final String QUEUE_LIVE_TICKS      = "live_ticks_queue";
    public static final String ROUTING_KEY_ALL_TICKS = "ticks.#";

    @Bean
    public TopicExchange marketTicksExchange() {
        return new TopicExchange(EXCHANGE_MARKET_TICKS, true, false);
    }

    @Bean
    public Queue liveTicsQueue() {
        return new Queue(QUEUE_LIVE_TICKS, true);
    }

    @Bean
    public Binding liveTicsBinding(Queue liveTicsQueue, TopicExchange marketTicksExchange) {
        return BindingBuilder
            .bind(liveTicsQueue)
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
}
