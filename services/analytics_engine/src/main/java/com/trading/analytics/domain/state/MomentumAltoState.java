package com.trading.analytics.domain.state;

import com.trading.analytics.domain.model.SignalPayload;

import java.util.Optional;

/**
 * Estado MOMENTUM_ALTO: la señal ya fue generada en este ciclo.
 * Transiciona inmediatamente a AlertaDisparadaState para indicar que la señal fue procesada.
 * No genera nuevas señales mientras el precio siga alto.
 */
public class MomentumAltoState implements AssetState {

    @Override
    public Optional<SignalPayload> onPrecioRecibido(AssetStateContext context,
                                                     double precio, double sma) {
        // La señal ya fue disparada. Transicionamos a AlertaDisparadaState
        // para registrar que el sistema procesó y notificó el evento.
        context.cambiarEstado(new AlertaDisparadaState());
        return Optional.empty();
    }

    @Override
    public String getNombre() {
        return "MOMENTUM_ALTO";
    }
}
