package com.trading.bridge.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Representa el tick de precio recibido desde RabbitMQ.
 * Contrato JSON exacto publicado por market_data_feed:
 *   {"asset":"BTC","price":59414.95,"timestamp":"2026-11-11T00:03:54Z"}
 */
public record TickEvent(
    @JsonProperty("asset") String asset,
    double price,
    Instant timestamp
) {
    public TickEvent {
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
