package com.trading.investdashboard.adapter.in;

import com.trading.investdashboard.application.SignalBroadcastService;
import com.trading.investdashboard.domain.model.SignalMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: consume señales del topic Kafka 'trading_signals'.
 * groupId: investment-dashboard-group (independiente de notification-router-group).
 */
@Component
public class SignalConsumer {

    private static final Logger log = LoggerFactory.getLogger(SignalConsumer.class);

    private final SignalBroadcastService broadcastService;

    public SignalConsumer(SignalBroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @KafkaListener(
        topics = "trading_signals",
        groupId = "${spring.kafka.consumer.group-id:investment-dashboard-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onSignal(SignalMessage signal) {
        log.info("Señal recibida de Kafka: asset={} signal={}", signal.asset(), signal.signal());
        broadcastService.procesar(signal);
    }
}
