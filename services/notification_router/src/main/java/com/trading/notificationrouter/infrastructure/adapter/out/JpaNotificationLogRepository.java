package com.trading.notificationrouter.infrastructure.adapter.out;

import com.trading.notificationrouter.domain.model.SignalEvent;
import com.trading.notificationrouter.domain.port.out.NotificationLogRepositoryPort;
import com.trading.notificationrouter.infrastructure.jpa.NotificationLogEntity;
import com.trading.notificationrouter.infrastructure.jpa.NotificationLogJpaRepository;
import com.trading.notificationrouter.infrastructure.jpa.TradingSignalLookupEntity;
import com.trading.notificationrouter.infrastructure.jpa.TradingSignalLookupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Adaptador de salida: persiste un registro en notification_log.
 *
 * Estrategia para resolver signal_id (corregida — Punto 1 de revisión técnica):
 *   Busca en trading_signals por asset + signal_type + current_price + sma_value.
 *   Los cuatro criterios juntos identifican unívocamente la señal recibida
 *   y evitan asociar la notificación con otra señal del mismo activo.
 *
 *   Si no encuentra la señal (ej. analytics_engine no persistió aún),
 *   loguea WARNING y omite la inserción. El despacho a RabbitMQ ya ocurrió.
 */
@Component
public class JpaNotificationLogRepository implements NotificationLogRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(JpaNotificationLogRepository.class);

    private final NotificationLogJpaRepository logRepo;
    private final TradingSignalLookupRepository signalRepo;

    public JpaNotificationLogRepository(NotificationLogJpaRepository logRepo,
                                        TradingSignalLookupRepository signalRepo) {
        this.logRepo    = logRepo;
        this.signalRepo = signalRepo;
    }

    @Override
    public void registrarDespacho(SignalEvent signal, String channel) {
        Optional<Long> signalId = resolverSignalId(signal);

        if (signalId.isEmpty()) {
            log.warn("No se encontró signal_id para asset={} price={} channel={} — omitiendo notification_log",
                signal.asset(), signal.price(), channel);
            return;
        }

        NotificationLogEntity entity = new NotificationLogEntity(
            signalId.get(),
            channel,
            Instant.now()
        );
        logRepo.save(entity);
        log.debug("Despacho registrado: signalId={} channel={}", signalId.get(), channel);
    }

    private Optional<Long> resolverSignalId(SignalEvent signal) {
        return signalRepo.findMostRecentMatch(
            signal.asset(),
            signal.signal(),
            BigDecimal.valueOf(signal.price()),
            BigDecimal.valueOf(signal.smaValue())
        ).map(TradingSignalLookupEntity::getId);
    }
}
