package com.trading.analytics.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Contrato de entrada: tick recibido desde Kafka topic 'historical_ticks'.
 * El JSON producido por ingestion_bridge no incluye __TypeId__ headers,
 * por lo que la deserialización usa el tipo explícito configurado en KafkaConsumerConfig.
 *
 * Formato exacto: {"asset":"BTC","price":59414.95,"timestamp":"2026-11-11T00:03:54Z"}
 */
public record TickRecord(
    @JsonProperty("asset") String asset,
    double price,
    Instant timestamp
) {
    public TickRecord {
        if (asset == null || asset.isBlank()) {
            throw new IllegalArgumentException("El activo no puede ser nulo ni vacío");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("El timestamp no puede ser nulo");
        }
    }
}
