package com.trading.investdashboard.domain.model;

/**
 * Contrato del mensaje recibido desde Kafka topic 'trading_signals'.
 * Publicado por analytics_engine con el formato de SignalPayload:
 *   {"asset":"BTC","signal":"BUY","reason":"Momentum Crossover","price":60043.71,"smaValue":43611.56}
 *
 * ADD_TYPE_INFO_HEADERS=false en el productor, por lo que el deserializador
 * usa el tipo explícito configurado en KafkaConsumerConfig.
 */
public record SignalMessage(
    String asset,
    String signal,
    String reason,
    double price,
    double smaValue
) {
    public SignalMessage {
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
