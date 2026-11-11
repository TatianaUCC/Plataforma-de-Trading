package com.trading.livedashboard.domain.port.out;

import com.trading.livedashboard.domain.model.TickMessage;

/**
 * Puerto de salida: desacopla el listener de RabbitMQ del mecanismo de broadcast.
 * La implementación concreta usa SimpMessagingTemplate (WebSocket/STOMP).
 */
public interface TickBroadcastPort {

    void broadcast(TickMessage tick);
}
