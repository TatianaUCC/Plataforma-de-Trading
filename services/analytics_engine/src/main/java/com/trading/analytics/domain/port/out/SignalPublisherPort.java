package com.trading.analytics.domain.port.out;

import com.trading.analytics.domain.model.SignalPayload;

/**
 * Puerto de salida: desacopla el dominio del cliente Kafka.
 * La implementación concreta publica en el topic 'trading_signals'.
 */
public interface SignalPublisherPort {

    void publicar(SignalPayload signal);
}
