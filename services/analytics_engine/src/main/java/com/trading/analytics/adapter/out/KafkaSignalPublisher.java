package com.trading.analytics.adapter.out;

import com.trading.analytics.domain.model.SignalPayload;
import com.trading.analytics.domain.port.out.SignalPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: publica señales en Kafka topic 'trading_signals'.
 * Usa el nombre del activo como partition key para mantener orden.
 *
 * El mensaje JSON publicado sigue el formato del enunciado:
 *   {"asset":"BTC","signal":"BUY","reason":"Momentum Crossover","price":...,"smaValue":...}
 *
 * Los consumidores (investment_dashboard_backend, notification_router) leerán
 * los campos que necesiten de este objeto.
 */
@Component
public class KafkaSignalPublisher implements SignalPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaSignalPublisher.class);
    private static final String TOPIC_TRADING_SIGNALS = "trading_signals";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaSignalPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publicar(SignalPayload signal) {
        kafkaTemplate.send(TOPIC_TRADING_SIGNALS, signal.asset(), signal);
        log.info("Señal publicada en Kafka: topic={} key={} signal={}",
            TOPIC_TRADING_SIGNALS, signal.asset(), signal.signal());
    }
}
