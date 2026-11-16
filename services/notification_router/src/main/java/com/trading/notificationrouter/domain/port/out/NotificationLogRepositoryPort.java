package com.trading.notificationrouter.domain.port.out;

import com.trading.notificationrouter.domain.model.SignalEvent;

/**
 * Puerto de salida: desacopla el dominio de la persistencia JPA de notification_log.
 */
public interface NotificationLogRepositoryPort {

    void registrarDespacho(SignalEvent signal, String channel);
}
