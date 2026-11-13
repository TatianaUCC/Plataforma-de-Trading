package com.trading.analytics.infrastructure.adapter.out;

import com.trading.analytics.domain.model.SignalPayload;
import com.trading.analytics.domain.port.out.SignalRepositoryPort;
import com.trading.analytics.infrastructure.jpa.TradingSignalEntity;
import com.trading.analytics.infrastructure.jpa.TradingSignalJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Adaptador de salida: implementa SignalRepositoryPort usando Spring Data JPA.
 * Convierte el SignalPayload del dominio a TradingSignalEntity para persistencia.
 */
@Component
public class JpaSignalRepository implements SignalRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(JpaSignalRepository.class);

    private final TradingSignalJpaRepository jpaRepository;

    public JpaSignalRepository(TradingSignalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void guardar(SignalPayload signal) {
        TradingSignalEntity entity = new TradingSignalEntity(
            signal.asset(),
            signal.signal(),
            signal.reason(),
            BigDecimal.valueOf(signal.price()),
            BigDecimal.valueOf(signal.smaValue()),
            Instant.now()
        );
        jpaRepository.save(entity);
        log.info("Señal persistida: asset={} signal={}", signal.asset(), signal.signal());
    }
}
