package com.trading.bridge.adapter.out;

import com.trading.bridge.domain.model.TickEvent;
import com.trading.bridge.domain.port.out.TickKafkaPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Adaptador de salida: publica cada tick en Kafka usando el activo como partition key.
 *
 * Estrategia de confirmación:
 *   - Se usa send().get(timeout) — espera bloqueante hasta que Kafka confirme.
 *   - Si Kafka no responde en 5 segundos o devuelve error, lanza RuntimeException.
 *   - El listener RabbitMQ recibe la excepción y ejecuta nack con requeue=true.
 *   - El tick se mantiene en la cola RabbitMQ y se reintenta en el siguiente ciclo.
 */
@Component
public class KafkaTickPublisher implements TickKafkaPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaTickPublisher.class);
    private static final String TOPIC_HISTORICAL_TICKS = "historical_ticks";
    private static final long SEND_TIMEOUT_SECONDS = 5;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaTickPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publicar(TickEvent tick) {
        String partitionKey = tick.asset();
        try {
            SendResult<String, Object> result = kafkaTemplate
                .send(TOPIC_HISTORICAL_TICKS, partitionKey, tick)
                .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            log.info("Tick publicado en Kafka: topic={} partición={} offset={} asset={}",
                result.getRecordMetadata().topic(),
                result.getRecordMetadata().partition(),
                result.getRecordMetadata().offset(),
                tick.asset());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Publicación Kafka interrumpida para activo: " + partitionKey, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("Error publicando tick en Kafka para activo: " + partitionKey, e);
        }
    }
}
