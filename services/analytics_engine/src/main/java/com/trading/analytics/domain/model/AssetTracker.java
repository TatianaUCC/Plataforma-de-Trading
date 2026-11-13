package com.trading.analytics.domain.model;

import com.trading.analytics.domain.state.AssetState;
import com.trading.analytics.domain.state.AssetStateContext;
import com.trading.analytics.domain.state.NormalState;
import com.trading.analytics.domain.strategy.IndicatorStrategy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Entidad de dominio rica que rastrea el estado y el historial de precios
 * de un activo financiero.
 *
 * Integra:
 *  - Patrón Strategy: delega el cálculo del indicador a IndicatorStrategy.
 *  - Patrón State: las transiciones de estado están encapsuladas en cada AssetState.
 *
 * Métodos semánticos (RT-01): registrarPrecio(), evaluarCruceMedia(), marcarNotificada().
 */
public class AssetTracker implements AssetStateContext {

    private final AssetCode assetCode;
    private final IndicatorStrategy strategy;
    private final Deque<Double> historialPrecios;
    private AssetState estadoActual;

    public AssetTracker(AssetCode assetCode, IndicatorStrategy strategy) {
        if (assetCode == null) throw new IllegalArgumentException("AssetCode no puede ser nulo");
        if (strategy == null) throw new IllegalArgumentException("Strategy no puede ser nula");
        this.assetCode = assetCode;
        this.strategy = strategy;
        this.historialPrecios = new ArrayDeque<>();
        this.estadoActual = new NormalState();
    }

    /**
     * Registra un nuevo precio en el historial.
     * Mantiene solo los últimos N precios según la ventana de la estrategia.
     */
    public void registrarPrecio(double precio) {
        if (precio <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero: " + precio);
        }
        historialPrecios.addLast(precio);
        while (historialPrecios.size() > strategy.ventanaMinima()) {
            historialPrecios.removeFirst();
        }
    }

    /**
     * Evalúa si el precio actual supera el umbral definido por la estrategia.
     * Solo evalúa si el historial tiene suficientes ticks (ventana mínima).
     * @return señal generada si se dispara momentum, vacío si no hay condición.
     */
    public Optional<SignalPayload> evaluarCruceMedia() {
        if (historialPrecios.size() < strategy.ventanaMinima()) {
            return Optional.empty();
        }

        List<Double> precios = new ArrayList<>(historialPrecios);
        double sma = strategy.calcular(precios);
        double precioActual = precios.get(precios.size() - 1);

        return estadoActual.onPrecioRecibido(this, precioActual, sma);
    }

    /**
     * Marca que la señal fue notificada externamente.
     * Semánticamente corresponde al paso del estado ALERTA_DISPARADA.
     */
    public void marcarNotificada() {
        estadoActual.onPrecioRecibido(this, 0, 0);
    }

    // --- AssetStateContext ---

    @Override
    public void cambiarEstado(AssetState nuevoEstado) {
        this.estadoActual = nuevoEstado;
    }

    @Override
    public String getAssetCode() {
        return assetCode.code();
    }

    // --- Consultas de solo lectura ---

    public String getEstadoActual() {
        return estadoActual.getNombre();
    }

    public int getTamanoHistorial() {
        return historialPrecios.size();
    }

    public AssetCode getAssetCodeObj() {
        return assetCode;
    }
}
