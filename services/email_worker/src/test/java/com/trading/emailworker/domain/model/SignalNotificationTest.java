package com.trading.emailworker.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignalNotificationTest {

    @Test
    void debeCrearseConDatosValidos() {
        SignalNotification n = new SignalNotification("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        assertThat(n.asset()).isEqualTo("BTC");
        assertThat(n.signal()).isEqualTo("BUY");
        assertThat(n.reason()).isEqualTo("Momentum Crossover");
        assertThat(n.price()).isEqualTo(60000.0);
    }

    @Test
    void debeFallarConActivoNulo() {
        assertThatThrownBy(() -> new SignalNotification(null, "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConActivoVacio() {
        assertThatThrownBy(() -> new SignalNotification("", "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConSignalNulo() {
        assertThatThrownBy(() -> new SignalNotification("BTC", null, "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConReasonNulo() {
        assertThatThrownBy(() -> new SignalNotification("BTC", "BUY", null, 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConPrecioCero() {
        assertThatThrownBy(() -> new SignalNotification("BTC", "BUY", "reason", 0.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConPrecioNegativo() {
        assertThatThrownBy(() -> new SignalNotification("ETH", "BUY", "reason", -1.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
