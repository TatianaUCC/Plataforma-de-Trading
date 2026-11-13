package com.trading.analytics.domain.model;

/**
 * Value Object inmutable que representa una señal de trading publicada en Kafka.
 * Formato JSON de salida en 'trading_signals':
 *   {"asset":"BTC","signal":"BUY","reason":"Momentum Crossover"}
 *
 * También contiene price y smaValue para persistencia en PostgreSQL,
 * pero solo asset, signal y reason van al mensaje Kafka según el enunciado.
 */
public record SignalPayload(
    String asset,
    String signal,
    String reason,
    double price,
    double smaValue
) {
    public SignalPayload {
        if (asset == null || asset.isBlank()) {
            throw new IllegalArgumentException("El activo no puede ser nulo ni vacío");
        }
        if (signal == null || signal.isBlank()) {
            throw new IllegalArgumentException("El tipo de señal no puede ser nulo ni vacío");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("La razón no puede ser nula ni vacía");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (smaValue <= 0) {
            throw new IllegalArgumentException("El valor SMA debe ser mayor que cero");
        }
    }
}
