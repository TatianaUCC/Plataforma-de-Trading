package com.trading.notificationrouter.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Proyección de solo lectura de la tabla 'trading_signals'.
 * El notification_router la consulta para obtener el signal_id
 * que necesita insertar en notification_log.
 *
 * Incluye current_price y sma_value para identificar con precisión
 * la señal exacta recibida por Kafka, evitando asociar la notificación
 * con una señal diferente del mismo activo generada en otro momento.
 *
 * Criterio de búsqueda: asset_code + signal_type + current_price + sma_value
 * Los cuatro campos juntos identifican unívocamente una señal.
 */
@Entity
@Table(name = "trading_signals")
public class TradingSignalLookupEntity {

    @Id
    private Long id;

    @Column(name = "asset_code")
    private String assetCode;

    @Column(name = "signal_type")
    private String signalType;

    @Column(name = "current_price", precision = 18, scale = 8)
    private BigDecimal currentPrice;

    @Column(name = "sma_value", precision = 18, scale = 8)
    private BigDecimal smaValue;

    @Column(name = "generated_at")
    private Instant generatedAt;

    protected TradingSignalLookupEntity() {}

    public Long getId()              { return id; }
    public String getAssetCode()     { return assetCode; }
    public String getSignalType()    { return signalType; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getSmaValue()  { return smaValue; }
    public Instant getGeneratedAt()  { return generatedAt; }
}
