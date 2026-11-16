package com.trading.consoleworker.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlertNotificationTest {

    @Test
    void debeCrearseConDatosValidos() {
        AlertNotification n = new AlertNotification("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        assertThat(n.asset()).isEqualTo("BTC");
        assertThat(n.signal()).isEqualTo("BUY");
        assertThat(n.price()).isEqualTo(60000.0);
    }

    @Test
    void debeFallarConActivoNulo() {
        assertThatThrownBy(() -> new AlertNotification(null, "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConActivoVacio() {
        assertThatThrownBy(() -> new AlertNotification("", "BUY", "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConSignalNulo() {
        assertThatThrownBy(() -> new AlertNotification("BTC", null, "reason", 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConReasonNulo() {
        assertThatThrownBy(() -> new AlertNotification("BTC", "BUY", null, 100.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConPrecioCero() {
        assertThatThrownBy(() -> new AlertNotification("ETH", "BUY", "reason", 0.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConPrecioNegativo() {
        assertThatThrownBy(() -> new AlertNotification("ETH", "BUY", "reason", -1.0, 90.0))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
