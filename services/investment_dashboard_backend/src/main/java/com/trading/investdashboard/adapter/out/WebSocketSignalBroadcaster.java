package com.trading.investdashboard.adapter.out;

import com.trading.investdashboard.domain.model.SignalMessage;
import com.trading.investdashboard.domain.port.out.SignalBroadcastPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida: publica señales en /topic/signals via STOMP/WebSocket.
 * El cliente investment.html está suscrito a este topic.
 */
@Component
public class WebSocketSignalBroadcaster implements SignalBroadcastPort {

    private static final Logger log = LoggerFactory.getLogger(WebSocketSignalBroadcaster.class);
    private static final String TOPIC_SIGNALS = "/topic/signals";

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketSignalBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcast(SignalMessage signal) {
        messagingTemplate.convertAndSend(TOPIC_SIGNALS, signal);
        log.debug("Señal enviada por WebSocket: {} = {}", signal.asset(), signal.signal());
    }
}
