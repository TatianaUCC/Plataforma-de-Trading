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
 * Entidad JPA que mapea la tabla 'tick_records'.
 * Registra cada tick de precio procesado por analytics_engine para auditoría.
 */
@Entity
@Table(name = "tick_records")
public class TickRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_code", nullable = false, length = 20)
    private String assetCode;

    @Column(name = "price", nullable = false, precision = 18, scale = 8)
    private BigDecimal price;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    protected TickRecordEntity() {}

    public TickRecordEntity(String assetCode, BigDecimal price, Instant processedAt) {
        if (assetCode == null || assetCode.isBlank()) {
            throw new IllegalArgumentException("asset_code no puede ser nulo ni vacío");
        }
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("price debe ser mayor que cero");
        }
        if (processedAt == null) {
            throw new IllegalArgumentException("processed_at no puede ser nulo");
        }
        this.assetCode = assetCode;
        this.price = price;
        this.processedAt = processedAt;
    }

    public Long getId() { return id; }
    public String getAssetCode() { return assetCode; }
    public BigDecimal getPrice() { return price; }
    public Instant getProcessedAt() { return processedAt; }
}
