package com.trading.bridge.application;

import com.trading.bridge.domain.model.TickEvent;
import com.trading.bridge.domain.port.out.TickKafkaPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación del puente RabbitMQ → Kafka.
 * Recibe un tick del listener RabbitMQ y lo publica en Kafka a través del puerto.
 *
 * Contrato de errores:
 *   - Si publicar(tick) lanza excepción, se propaga al listener RabbitMQ.
 *   - El listener RabbitMQ opera con ACK manual: solo confirma el mensaje
 *     después de que este método retorne sin excepción.
 *   - Ante fallo de Kafka, el mensaje RabbitMQ es reencolar (nack con requeue=true).
 */
@Service
public class BridgeService {

    private static final Logger log = LoggerFactory.getLogger(BridgeService.class);

    private final TickKafkaPublisherPort kafkaPublisher;

    public BridgeService(TickKafkaPublisherPort kafkaPublisher) {
        this.kafkaPublisher = kafkaPublisher;
    }

    public void transferir(TickEvent tick) {
        log.debug("Transfiriendo tick a Kafka: {} = {}", tick.asset(), tick.price());
        kafkaPublisher.publicar(tick);
    }
}
