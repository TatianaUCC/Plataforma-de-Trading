package com.trading.analytics.application;

import com.trading.analytics.domain.model.AssetCode;
import com.trading.analytics.domain.model.AssetTracker;
import com.trading.analytics.domain.model.SignalPayload;
import com.trading.analytics.domain.model.TickRecord;
import com.trading.analytics.domain.port.out.SignalPublisherPort;
import com.trading.analytics.domain.port.out.SignalRepositoryPort;
import com.trading.analytics.domain.port.out.TickRecordRepositoryPort;
import com.trading.analytics.domain.strategy.IndicatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio de aplicación del motor analítico.
 *
 * Mantiene un AssetTracker por activo en memoria (ConcurrentHashMap).
 * Por cada tick recibido:
 *   1. Registra el precio en el tracker del activo.
 *   2. Evalúa si se cruza el umbral de momentum.
 *   3. Si hay señal, la publica a Kafka y la persiste en PostgreSQL.
 *   4. Persiste el tick en PostgreSQL para auditoría.
 *
 * Manejo de errores de persistencia:
 *   - Si la persistencia falla, se registra el error pero el procesamiento continúa.
 *   - El análisis y la publicación de señales son funciones primarias.
 *   - La persistencia es auditoría: un fallo no debe detener el flujo.
 *   - El error se registra a nivel ERROR (no es silencioso).
 */
@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final IndicatorStrategy strategy;
    private final SignalPublisherPort signalPublisher;
    private final SignalRepositoryPort signalRepository;
    private final TickRecordRepositoryPort tickRepository;
    private final Map<String, AssetTracker> trackers = new ConcurrentHashMap<>();

    public AnalyticsService(IndicatorStrategy strategy,
                            SignalPublisherPort signalPublisher,
                            SignalRepositoryPort signalRepository,
                            TickRecordRepositoryPort tickRepository) {
        this.strategy = strategy;
        this.signalPublisher = signalPublisher;
        this.signalRepository = signalRepository;
        this.tickRepository = tickRepository;
    }

    public void procesarTick(TickRecord tick) {
        String assetKey = tick.asset().toUpperCase();
        AssetTracker tracker = trackers.computeIfAbsent(
            assetKey,
            asset -> new AssetTracker(new AssetCode(asset), strategy)
        );

        tracker.registrarPrecio(tick.price());

        Optional<SignalPayload> signal = tracker.evaluarCruceMedia();
        signal.ifPresent(s -> {
            log.info("Señal generada: asset={} signal={} precio={} sma={}",
                s.asset(), s.signal(), s.price(), s.smaValue());
            signalPublisher.publicar(s);
            persistirSenal(s);
        });

        persistirTick(tick);
    }

    private void persistirSenal(SignalPayload signal) {
        try {
            signalRepository.guardar(signal);
        } catch (Exception e) {
            log.error("Error persistiendo señal {} — auditoría incompleta", signal.asset(), e);
        }
    }

    private void persistirTick(TickRecord tick) {
        try {
            tickRepository.guardar(tick);
        } catch (Exception e) {
            log.error("Error persistiendo tick {} — auditoría incompleta", tick.asset(), e);
        }
    }

    /** Expuesto para pruebas y monitoreo — no modifica estado. */
    public String getEstado(String asset) {
        AssetTracker tracker = trackers.get(asset.toUpperCase());
        return tracker != null ? tracker.getEstadoActual() : "SIN_DATOS";
    }
}
