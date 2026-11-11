package com.trading.marketfeed.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Value Object inmutable que representa un tick de precio de mercado.
 * Validación Fail-Fast: precio debe ser mayor que cero.
 *
 * JSON publicado en RabbitMQ:
 *   {"asset":"BTC","price":60000.0,"timestamp":"2026-11-10T23:52:53Z"}
 */
public record TickPriceRecord(
    @JsonProperty("asset") AssetCode assetCode,
    double price,
    Instant timestamp
) {

    public TickPriceRecord {
        if (assetCode == null) {
            throw new IllegalArgumentException("El código del activo no puede ser nulo");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero, recibido: " + price);
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("El timestamp no puede ser nulo");
        }
    }
}
