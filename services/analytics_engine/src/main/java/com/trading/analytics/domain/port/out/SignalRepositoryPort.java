package com.trading.analytics.domain.port.out;

import com.trading.analytics.domain.model.SignalPayload;

/**
 * Puerto de salida: desacopla el dominio del repositorio JPA de señales.
 */
public interface SignalRepositoryPort {

    void guardar(SignalPayload signal);
}
