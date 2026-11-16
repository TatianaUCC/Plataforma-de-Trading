package com.trading.consoleworker.domain.model;

/**
 * Contrato del mensaje recibido desde RabbitMQ Fanout Exchange 'console_alerts'.
 * Publicado por notification_router con el mismo formato de SignalEvent:
 *   {"asset":"BTC","signal":"BUY","reason":"Momentum Crossover","price":60000.0,"smaValue":43000.0}
 */
public record AlertNotification(
    String asset,
    String signal,
    String reason,
    double price,
    double smaValue
) {
    public AlertNotification {
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
