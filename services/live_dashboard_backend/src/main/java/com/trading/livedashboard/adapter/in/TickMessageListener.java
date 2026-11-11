package com.trading.livedashboard.adapter.in;

import com.trading.livedashboard.config.RabbitMQConfig;
import com.trading.livedashboard.domain.model.TickMessage;
import com.trading.livedashboard.domain.port.out.TickBroadcastPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Adaptador de entrada: consume ticks de RabbitMQ y los delega al puerto de broadcast.
 * No contiene lógica de negocio ni acceso a BD — Hot-Path puro.
 */
@Component
public class TickMessageListener {

    private static final Logger log = LoggerFactory.getLogger(TickMessageListener.class);

    private final TickBroadcastPort broadcastPort;

    public TickMessageListener(TickBroadcastPort broadcastPort) {
        this.broadcastPort = broadcastPort;
    }

    @RabbitListener(queues = RabbitMQConfig.QUEUE_LIVE_TICKS)
    public void onTick(TickMessage tick) {
        log.info("Tick recibido: {} = {}", tick.asset(), tick.price());
        broadcastPort.broadcast(tick);
    }
}
