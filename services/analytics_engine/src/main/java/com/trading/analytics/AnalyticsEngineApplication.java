package com.trading.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.trading.analytics.domain.strategy.IndicatorStrategy;
import com.trading.analytics.domain.strategy.SimpleMovingAverageStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Motor de análisis (Cold-Path).
 * Consume de Kafka 'historical_ticks', calcula SMA, genera señales en 'trading_signals'.
 *
 * Nota: PostgreSQL está configurado pero la persistencia JPA de señales y ticks
 * se implementa en una etapa posterior. Esta etapa se centra en el dominio y Kafka.
 * El pom.xml incluye JPA/PostgreSQL porque el servicio depende de ellos en runtime
 * (Spring Boot autoconfigura el datasource), pero hibernate.ddl-auto=none evita
 * que JPA intente escribir sin esquema.
 */
@SpringBootApplication
public class AnalyticsEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsEngineApplication.class, args);
    }

    @Bean
    public IndicatorStrategy indicatorStrategy() {
        return new SimpleMovingAverageStrategy();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
