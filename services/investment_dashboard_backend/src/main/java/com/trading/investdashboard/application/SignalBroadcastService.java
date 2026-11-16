package com.trading.investdashboard.application;

import com.trading.investdashboard.domain.model.SignalMessage;
import com.trading.investdashboard.domain.port.out.SignalBroadcastPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación del investment_dashboard_backend.
 * Recibe la señal del consumer Kafka y la retransmite por WebSocket.
 */
@Service
public class SignalBroadcastService {

    private static final Logger log = LoggerFactory.getLogger(SignalBroadcastService.class);

    private final SignalBroadcastPort broadcastPort;

    public SignalBroadcastService(SignalBroadcastPort broadcastPort) {
        this.broadcastPort = broadcastPort;
    }

    public void procesar(SignalMessage signal) {
        log.info("Señal recibida para dashboard: asset={} signal={}", signal.asset(), signal.signal());
        broadcastPort.broadcast(signal);
    }
}
