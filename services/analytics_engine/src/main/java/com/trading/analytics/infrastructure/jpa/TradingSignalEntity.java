package com.trading.analytics.infrastructure.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad JPA que mapea la tabla 'trading_signals'.
 * Registra cada señal de inversión generada por analytics_engine.
 */
@Entity
@Table(name = "trading_signals")
public class TradingSignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_code", nullable = false, length = 20)
    private String assetCode;

    @Column(name = "signal_type", nullable = false, length = 10)
    private String signalType;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;

    @Column(name = "current_price", nullable = false, precision = 18, scale = 8)
    private BigDecimal currentPrice;

    @Column(name = "sma_value", nullable = false, precision = 18, scale = 8)
    private BigDecimal smaValue;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    @Column(name = "notified", nullable = false)
    private boolean notified = false;

    protected TradingSignalEntity() {}

    public TradingSignalEntity(String assetCode, String signalType, String reason,
                               BigDecimal currentPrice, BigDecimal smaValue,
                               Instant generatedAt) {
        if (assetCode == null || assetCode.isBlank()) {
            throw new IllegalArgumentException("asset_code no puede ser nulo ni vacío");
        }
        if (signalType == null || signalType.isBlank()) {
            throw new IllegalArgumentException("signal_type no puede ser nulo ni vacío");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason no puede ser nula ni vacía");
        }
        if (currentPrice == null || currentPrice.signum() <= 0) {
            throw new IllegalArgumentException("current_price debe ser mayor que cero");
        }
        if (smaValue == null || smaValue.signum() <= 0) {
            throw new IllegalArgumentException("sma_value debe ser mayor que cero");
        }
        if (generatedAt == null) {
            throw new IllegalArgumentException("generated_at no puede ser nulo");
        }
        this.assetCode = assetCode;
        this.signalType = signalType;
        this.reason = reason;
        this.currentPrice = currentPrice;
        this.smaValue = smaValue;
        this.generatedAt = generatedAt;
    }

    public Long getId() { return id; }
    public String getAssetCode() { return assetCode; }
    public String getSignalType() { return signalType; }
    public String getReason() { return reason; }
    public BigDecimal getCurrentPrice() { return currentPrice; }
    public BigDecimal getSmaValue() { return smaValue; }
    public Instant getGeneratedAt() { return generatedAt; }
    public boolean isNotified() { return notified; }

    public void marcarNotificada() {
        this.notified = true;
    }
}
