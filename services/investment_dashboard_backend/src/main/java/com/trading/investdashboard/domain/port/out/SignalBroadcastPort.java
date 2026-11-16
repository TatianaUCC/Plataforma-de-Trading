package com.trading.investdashboard.domain.port.out;

import com.trading.investdashboard.domain.model.SignalMessage;

/**
 * Puerto de salida: desacopla el dominio del mecanismo de broadcast WebSocket.
 */
public interface SignalBroadcastPort {

    void broadcast(SignalMessage signal);
}
