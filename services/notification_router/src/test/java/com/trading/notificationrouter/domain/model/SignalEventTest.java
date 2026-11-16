package com.trading.notificationrouter.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignalEventTest {

    @Test
    void debeCrearseConDatosValidos() {
        SignalEvent event = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        assertThat(event.asset()).isEqualTo("BTC");
        assertThat(event.signal()).isEqualTo("BUY");
        assertThat(event.reason()).isEqualTo("Momentum Crossover");
    }

    @Test
    void debeFallarConActivoNulo() {
        assertThatThrownBy(() -> new SignalEvent(null, "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConActivoVacio() {
        assertThatThrownBy(() -> new SignalEvent("", "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConSignalNulo() {
        assertThatThrownBy(() -> new SignalEvent("BTC", null, "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConReasonNulo() {
        assertThatThrownBy(() -> new SignalEvent("BTC", "BUY", null, 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConPrecioCero() {
        assertThatThrownBy(() -> new SignalEvent("BTC", "BUY", "reason", 0.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConPrecioNegativo() {
        assertThatThrownBy(() -> new SignalEvent("ETH", "BUY", "reason", -1.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
