package com.trading.analytics.domain.state;

import com.trading.analytics.domain.model.SignalPayload;

import java.util.Optional;

/**
 * Estado ALERTA_DISPARADA: la señal fue procesada y notificada.
 * Vuelve a NormalState en el siguiente tick para permitir detectar
 * nuevas oportunidades de momentum.
 *
 * Nota de diseño: el enunciado no especifica explícitamente cuándo
 * se regresa a NORMAL. Se elige el siguiente tick como la opción más
 * simple y funcional para el contexto académico.
 */
public class AlertaDisparadaState implements AssetState {

    @Override
    public Optional<SignalPayload> onPrecioRecibido(AssetStateContext context,
                                                     double precio, double sma) {
        // Regresar a NORMAL para estar listo para la próxima detección
        context.cambiarEstado(new NormalState());
        return Optional.empty();
    }

    @Override
    public String getNombre() {
        return "ALERTA_DISPARADA";
    }
}
