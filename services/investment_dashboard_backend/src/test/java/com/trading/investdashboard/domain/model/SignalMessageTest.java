package com.trading.investdashboard.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SignalMessageTest {

    @Test
    void debeCrearseConDatosValidos() {
        SignalMessage msg = new SignalMessage("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        assertThat(msg.asset()).isEqualTo("BTC");
        assertThat(msg.signal()).isEqualTo("BUY");
        assertThat(msg.reason()).isEqualTo("Momentum Crossover");
        assertThat(msg.price()).isEqualTo(60000.0);
        assertThat(msg.smaValue()).isEqualTo(43000.0);
    }

    @Test
    void debeFallarConActivoNulo() {
        assertThatThrownBy(() -> new SignalMessage(null, "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConActivoVacio() {
        assertThatThrownBy(() -> new SignalMessage("", "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConSignalNulo() {
        assertThatThrownBy(() -> new SignalMessage("BTC", null, "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConReasonNulo() {
        assertThatThrownBy(() -> new SignalMessage("BTC", "BUY", null, 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConPrecioCero() {
        assertThatThrownBy(() -> new SignalMessage("BTC", "BUY", "reason", 0.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConPrecioNegativo() {
        assertThatThrownBy(() -> new SignalMessage("ETH", "BUY", "reason", -1.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
