package com.trading.analytics.domain.state;

import com.trading.analytics.domain.model.SignalPayload;

import java.util.Optional;

/**
 * Estado NORMAL: el activo no muestra momentum significativo.
 * Si el precio supera SMA * 1.05, transiciona a MomentumAltoState y genera señal BUY.
 * Si la SMA aún no tiene suficientes ticks (sma <= 0), no evalúa.
 */
public class NormalState implements AssetState {

    private static final double UMBRAL = 1.05;

    @Override
    public Optional<SignalPayload> onPrecioRecibido(AssetStateContext context,
                                                     double precio, double sma) {
        if (sma <= 0) {
            return Optional.empty();
        }

        if (precio > sma * UMBRAL) {
            context.cambiarEstado(new MomentumAltoState());
            SignalPayload signal = new SignalPayload(
                context.getAssetCode(),
                "BUY",
                "Momentum Crossover",
                precio,
                sma
            );
            return Optional.of(signal);
        }

        return Optional.empty();
    }

    @Override
    public String getNombre() {
        return "NORMAL";
    }
}
