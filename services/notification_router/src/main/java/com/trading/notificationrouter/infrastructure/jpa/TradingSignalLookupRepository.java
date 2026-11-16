package com.trading.notificationrouter.infrastructure.jpa;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de solo lectura sobre trading_signals.
 *
 * Criterio de búsqueda: asset_code + signal_type + current_price (±0.00001) + sma_value (±0.00001).
 * Los cuatro criterios juntos identifican unívocamente la señal recibida por Kafka
 * y evitan asociar la notificación con una señal diferente del mismo activo.
 *
 * Nota sobre generatedAt: el contrato Kafka (SignalEvent) no incluye ese campo,
 * por lo que no puede usarse como criterio de búsqueda sin modificar el contrato.
 * Con precio + SMA la discriminación ya es prácticamente unívoca.
 *
 * LIMIT: se reemplaza por Pageable (Spring Data JPA estándar) en lugar de
 * LIMIT en JPQL, que aunque Hibernate 6 lo acepta, no es parte del estándar JPQL.
 */
@Repository
public interface TradingSignalLookupRepository extends JpaRepository<TradingSignalLookupEntity, Long> {

    /**
     * Devuelve las señales que coinciden con los 4 criterios, ordenadas por generated_at DESC.
     * El llamador toma el primer resultado (el más reciente que coincida).
     * Se pasa un Pageable con size=1 para limitar a un resultado en la query SQL generada.
     */
    @Query("""
        SELECT s FROM TradingSignalLookupEntity s
        WHERE s.assetCode  = :asset
          AND s.signalType = :signalType
          AND ABS(s.currentPrice - :price)   < 0.00001
          AND ABS(s.smaValue     - :smaValue) < 0.00001
        """)
    List<TradingSignalLookupEntity> findMatchingSignals(
        @Param("asset")      String asset,
        @Param("signalType") String signalType,
        @Param("price")      BigDecimal price,
        @Param("smaValue")   BigDecimal smaValue,
        org.springframework.data.domain.Pageable pageable
    );

    /**
     * Método de conveniencia: devuelve la señal más reciente que coincida.
     */
    default Optional<TradingSignalLookupEntity> findMostRecentMatch(
            String asset, String signalType, BigDecimal price, BigDecimal smaValue) {
        List<TradingSignalLookupEntity> results = findMatchingSignals(
            asset, signalType, price, smaValue,
            PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "generatedAt"))
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
