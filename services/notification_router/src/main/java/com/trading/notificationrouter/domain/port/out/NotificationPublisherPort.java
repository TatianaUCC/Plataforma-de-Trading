package com.trading.notificationrouter.domain.port.out;

import com.trading.notificationrouter.domain.model.SignalEvent;

/**
 * Puerto de salida: desacopla el dominio del cliente RabbitMQ.
 * Publica la señal en email_queue (Work Queue) y console_alerts (Fanout).
 */
public interface NotificationPublisherPort {

    void publicar(SignalEvent signal);
}
