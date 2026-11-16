package com.trading.notificationrouter.adapter.out;

import com.trading.notificationrouter.config.RabbitMQConfig;
import com.trading.notificationrouter.domain.model.SignalEvent;
import com.trading.notificationrouter.domain.port.out.NotificationPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: publica señales en RabbitMQ.
 *
 * Destinos:
 *   1. email_queue (Work Queue) — un solo email_worker lo procesa (balanceo)
 *   2. console_alerts (Fanout Exchange) — todos los console_alert_worker lo reciben
 */
@Component
public class RabbitMQNotificationPublisher implements NotificationPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQNotificationPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQNotificationPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(SignalEvent signal) {
        // Work Queue: sin exchange (default exchange), routing key = nombre de la cola
        rabbitTemplate.convertAndSend(
            "",
            RabbitMQConfig.QUEUE_EMAIL,
            signal
        );
        log.info("Señal enviada a email_queue: asset={}", signal.asset());

        // Fanout Exchange: routing key ignorada
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_CONSOLE_ALERTS,
            "",
            signal
        );
        log.info("Señal enviada a console_alerts: asset={}", signal.asset());
    }
}
