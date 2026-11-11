package com.trading.marketfeed.domain.port.out;

import com.trading.marketfeed.domain.model.TickPriceRecord;

/**
 * Puerto de salida (DIP): desacopla la lógica de dominio del cliente RabbitMQ.
 * La implementación concreta vive en la capa de infraestructura.
 */
public interface TickPublisherPort {

    void publicar(TickPriceRecord tick);
}
