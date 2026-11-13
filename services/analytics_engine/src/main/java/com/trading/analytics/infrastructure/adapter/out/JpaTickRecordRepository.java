package com.trading.analytics.infrastructure.adapter.out;

import com.trading.analytics.domain.model.TickRecord;
import com.trading.analytics.domain.port.out.TickRecordRepositoryPort;
import com.trading.analytics.infrastructure.jpa.TickRecordEntity;
import com.trading.analytics.infrastructure.jpa.TickRecordJpaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adaptador de salida: implementa TickRecordRepositoryPort usando Spring Data JPA.
 * Convierte el TickRecord del dominio a TickRecordEntity para persistencia.
 */
@Component
public class JpaTickRecordRepository implements TickRecordRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(JpaTickRecordRepository.class);

    private final TickRecordJpaRepository jpaRepository;

    public JpaTickRecordRepository(TickRecordJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void guardar(TickRecord tick) {
        TickRecordEntity entity = new TickRecordEntity(
            tick.asset(),
            BigDecimal.valueOf(tick.price()),
            tick.timestamp()
        );
        jpaRepository.save(entity);
        log.debug("Tick persistido: asset={} price={}", tick.asset(), tick.price());
    }
}
