package com.trading.analytics.domain.model;

import com.trading.analytics.domain.strategy.SimpleMovingAverageStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetTrackerTest {

    private AssetTracker tracker;

    @BeforeEach
    void setUp() {
        tracker = new AssetTracker(new AssetCode("BTC"), new SimpleMovingAverageStrategy());
    }

    @Test
    void estadoInicialEsNormal() {
        assertThat(tracker.getEstadoActual()).isEqualTo("NORMAL");
    }

    @Test
    void registrarPrecioAumentaHistorial() {
        tracker.registrarPrecio(60000.0);
        assertThat(tracker.getTamanoHistorial()).isEqualTo(1);
    }

    @Test
    void registrarPrecioFallaConPrecioNegativo() {
        assertThatThrownBy(() -> tracker.registrarPrecio(-1.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registrarPrecioFallaConPrecioCero() {
        assertThatThrownBy(() -> tracker.registrarPrecio(0.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void noGeneraSenalConMenosDe5Ticks() {
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(110.0);
        tracker.registrarPrecio(120.0);

        Optional<SignalPayload> signal = tracker.evaluarCruceMedia();
        assertThat(signal).isEmpty();
    }

    @Test
    void noGeneraSenalCuandoPrecioNoSuperaUmbral() {
        // Precios planos: SMA = 100, precio = 100 → 100 < 100*1.05
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);

        Optional<SignalPayload> signal = tracker.evaluarCruceMedia();
        assertThat(signal).isEmpty();
    }

    @Test
    void generaSenalCuandoPrecioSupera5PorCientoSobreSMA() {
        // SMA de 5 precios = 100.0
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);

        // Ahora registramos un sexto precio que supera SMA*1.05
        // Como la ventana es 5, el historial guarda solo los últimos 5.
        // El sexto tick desplaza el primero: historial = [100,100,100,100,precioNuevo]
        // SMA = (100*4 + precioNuevo) / 5
        // Para que precioNuevo > SMA * 1.05 necesitamos precioNuevo suficientemente alto.
        // Pero evaluarCruceMedia usa el último precio del historial.
        // Registramos el sexto precio y evaluamos:
        tracker.registrarPrecio(200.0);
        // historial = [100,100,100,100,200] → SMA = 120 → 200 > 126? Si.
        Optional<SignalPayload> signal = tracker.evaluarCruceMedia();

        assertThat(signal).isPresent();
        assertThat(signal.get().asset()).isEqualTo("BTC");
        assertThat(signal.get().signal()).isEqualTo("BUY");
        assertThat(signal.get().reason()).isEqualTo("Momentum Crossover");
    }

    @Test
    void despuesDeSenalTransicionaAMomentumAlto() {
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(200.0);

        tracker.evaluarCruceMedia();

        assertThat(tracker.getEstadoActual()).isEqualTo("MOMENTUM_ALTO");
    }

    @Test
    void siguienteTickDespuesDeMomentumAltoVaAAlertaDisparada() {
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(200.0);
        tracker.evaluarCruceMedia(); // → MOMENTUM_ALTO

        tracker.registrarPrecio(150.0);
        tracker.evaluarCruceMedia(); // → ALERTA_DISPARADA

        assertThat(tracker.getEstadoActual()).isEqualTo("ALERTA_DISPARADA");
    }

    @Test
    void despuesDeAlertaDisparadaVuelveANormal() {
        // Llevamos hasta ALERTA_DISPARADA
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(100.0);
        tracker.registrarPrecio(200.0);
        tracker.evaluarCruceMedia(); // MOMENTUM_ALTO
        tracker.registrarPrecio(150.0);
        tracker.evaluarCruceMedia(); // ALERTA_DISPARADA

        // Siguiente tick
        tracker.registrarPrecio(100.0);
        tracker.evaluarCruceMedia();

        assertThat(tracker.getEstadoActual()).isEqualTo("NORMAL");
    }

    @Test
    void mantieneSoloUltimos5Precios() {
        tracker.registrarPrecio(10.0);
        tracker.registrarPrecio(20.0);
        tracker.registrarPrecio(30.0);
        tracker.registrarPrecio(40.0);
        tracker.registrarPrecio(50.0);
        tracker.registrarPrecio(60.0);

        assertThat(tracker.getTamanoHistorial()).isEqualTo(5);
    }
}
