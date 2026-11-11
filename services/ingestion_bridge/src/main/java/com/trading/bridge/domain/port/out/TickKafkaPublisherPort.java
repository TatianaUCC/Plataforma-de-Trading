package com.trading.bridge.domain.port.out;

import com.trading.bridge.domain.model.TickEvent;

/**
 * Puerto de salida: desacopla el servicio de aplicación del cliente Kafka.
 * Contrato: si el método retorna sin excepción, el tick fue confirmado por Kafka.
 * Si lanza excepción, el llamador debe decidir si reencola o descarta.
 */
public interface TickKafkaPublisherPort {

    void publicar(TickEvent tick);
}
