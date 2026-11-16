package com.trading.notificationrouter.domain.model;

/**
 * Contrato del mensaje recibido desde Kafka topic 'trading_signals'.
 * Publicado por analytics_engine con todos los campos de SignalPayload:
 *   {"asset":"BTC","signal":"BUY","reason":"Momentum Crossover","price":60000.0,"smaValue":43611.0}
 *
 * Se recibe como String+String en Kafka (sin __TypeId__ header),
 * por lo que el deserializador usa el tipo explícito configurado.
 */
public record SignalEvent(
    String asset,
    String signal,
    String reason,
    double price,
    double smaValue
) {
    public SignalEvent {
        if (asset == null || asset.isBlank()) {
            throw new IllegalArgumentException("asset no puede ser nulo ni vacío");
        }
        if (signal == null || signal.isBlank()) {
            throw new IllegalArgumentException("signal no puede ser nulo ni vacío");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason no puede ser nulo ni vacío");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("price debe ser mayor que cero");
        }
    }
}
