package com.trading.notificationrouter.application;

import com.trading.notificationrouter.domain.model.SignalEvent;
import com.trading.notificationrouter.domain.port.out.NotificationLogRepositoryPort;
import com.trading.notificationrouter.domain.port.out.NotificationPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación del notification_router.
 *
 * Por cada señal recibida:
 *   1. Publica en RabbitMQ (email_queue y console_alerts).
 *   2. Registra el despacho en notification_log para auditoría.
 *
 * Estrategia de errores:
 *   - Si RabbitMQ falla → propaga excepción al listener Kafka (no hace ACK → reintenta).
 *   - Si la persistencia falla → se loguea ERROR, el despacho ya ocurrió (best-effort).
 *   - Posibilidad de duplicación: si el servicio falla después de publicar en RabbitMQ
 *     pero antes del ACK Kafka, el mensaje se reintenta → duplicado en workers.
 *     Este comportamiento es inherente a la arquitectura at-least-once y se documenta.
 */
@Service
public class NotificationRouterService {

    private static final Logger log = LoggerFactory.getLogger(NotificationRouterService.class);

    private final NotificationPublisherPort publisher;
    private final NotificationLogRepositoryPort logRepository;

    public NotificationRouterService(NotificationPublisherPort publisher,
                                     NotificationLogRepositoryPort logRepository) {
        this.publisher = publisher;
        this.logRepository = logRepository;
    }

    public void enrutar(SignalEvent signal) {
        log.info("Enrutando señal: asset={} signal={}", signal.asset(), signal.signal());

        // 1. Publicar en RabbitMQ — si falla, se propaga y Kafka no hace ACK
        publisher.publicar(signal);

        // 2. Registrar auditoría en PostgreSQL — fallo no detiene el flujo ya completado
        registrarDespacho(signal, "EMAIL");
        registrarDespacho(signal, "CONSOLE");
    }

    private void registrarDespacho(SignalEvent signal, String channel) {
        try {
            logRepository.registrarDespacho(signal, channel);
        } catch (Exception e) {
            log.error("Error registrando despacho en notification_log: asset={} channel={}", 
                signal.asset(), channel, e);
        }
    }
}
