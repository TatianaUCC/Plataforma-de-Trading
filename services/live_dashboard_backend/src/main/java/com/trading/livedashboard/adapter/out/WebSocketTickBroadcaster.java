package com.trading.livedashboard.adapter.out;

import com.trading.livedashboard.domain.model.TickMessage;
import com.trading.livedashboard.domain.port.out.TickBroadcastPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: implementa TickBroadcastPort usando STOMP/WebSocket.
 * Publica cada tick en el topic '/topic/ticks' al que está suscrito el dashboard.
 */
@Component
public class WebSocketTickBroadcaster implements TickBroadcastPort {

    private static final Logger log = LoggerFactory.getLogger(WebSocketTickBroadcaster.class);
    private static final String TOPIC_TICKS = "/topic/ticks";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketTickBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcast(TickMessage tick) {
        messagingTemplate.convertAndSend(TOPIC_TICKS, tick);
        log.debug("Broadcast WebSocket: {} = {}", tick.asset(), tick.price());
    }
}
