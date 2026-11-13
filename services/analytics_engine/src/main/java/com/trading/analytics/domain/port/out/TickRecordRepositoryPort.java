package com.trading.analytics.domain.port.out;

import com.trading.analytics.domain.model.TickRecord;

/**
 * Puerto de salida: desacopla el dominio del repositorio JPA.
 * Si el guardado falla, lanza RuntimeException — el llamador decide cómo manejarlo.
 */
public interface TickRecordRepositoryPort {

    void guardar(TickRecord tick);
}
