package com.trading.notificationrouter.adapter.in;

import com.trading.notificationrouter.application.NotificationRouterService;
import com.trading.notificationrouter.domain.model.SignalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: consume señales del topic Kafka 'trading_signals'.
 * groupId: notification-router-group (independiente de investment-dashboard-group).
 *
 * Estrategia de errores:
 *   - Si el servicio lanza excepción (fallo RabbitMQ), el listener propaga el error.
 *   - Spring Kafka con enable.auto.commit=true confirma después del procesamiento.
 *   - Ante fallo, el mensaje puede reintentarse (at-least-once delivery).
 *   - Posibilidad de duplicación documentada en NotificationRouterService.
 */
@Component
public class SignalKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(SignalKafkaConsumer.class);

    private final NotificationRouterService routerService;

    public SignalKafkaConsumer(NotificationRouterService routerService) {
        this.routerService = routerService;
    }

    @KafkaListener(
        topics = "trading_signals",
        groupId = "${spring.kafka.consumer.group-id:notification-router-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSignal(SignalEvent signal) {
        log.info("Señal recibida de Kafka: asset={} signal={}", signal.asset(), signal.signal());
        routerService.enrutar(signal);
    }
}
