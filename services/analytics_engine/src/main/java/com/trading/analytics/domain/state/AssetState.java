package com.trading.analytics.domain.state;

import com.trading.analytics.domain.model.SignalPayload;

import java.util.Optional;

/**
 * Interfaz del patrón State para los estados del activo financiero.
 * Cada implementación encapsula la lógica de transición sin if-else externo.
 */
public interface AssetState {

    /**
     * Procesa un nuevo precio y decide si transiciona a otro estado.
     * @param context el AssetTracker que posee el estado
     * @param precio  precio del tick recibido
     * @param sma     valor SMA calculado (0 si no hay suficientes ticks)
     * @return señal generada si corresponde, vacío si no
     */
    Optional<SignalPayload> onPrecioRecibido(AssetStateContext context, double precio, double sma);

    String getNombre();
}
