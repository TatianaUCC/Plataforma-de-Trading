package com.trading.analytics.adapter.in;

import com.trading.analytics.application.AnalyticsService;
import com.trading.analytics.domain.model.TickRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: consume ticks del topic Kafka 'historical_ticks'
 * y los delega al servicio de análisis.
 *
 * groupId 'analytics-engine-group' garantiza que este servicio reciba
 * todos los mensajes del topic de forma independiente de otros consumidores.
 */
@Component
public class HistoricalTickConsumer {

    private static final Logger log = LoggerFactory.getLogger(HistoricalTickConsumer.class);

    private final AnalyticsService analyticsService;

    public HistoricalTickConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @KafkaListener(
        topics = "historical_ticks",
        groupId = "${spring.kafka.consumer.group-id:analytics-engine-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTick(TickRecord tick) {
        log.debug("Tick recibido desde Kafka: {} = {}", tick.asset(), tick.price());
        analyticsService.procesarTick(tick);
    }
}
