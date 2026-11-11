package com.trading.bridge.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TickEventTest {

    @Test
    void debeCrearseConDatosValidos() {
        Instant now = Instant.now();
        TickEvent event = new TickEvent("BTC", 60_000.0, now);

        assertThat(event.asset()).isEqualTo("BTC");
        assertThat(event.price()).isEqualTo(60_000.0);
        assertThat(event.timestamp()).isEqualTo(now);
    }

    @Test
    void debeFallarConActivoNulo() {
        assertThatThrownBy(() -> new TickEvent(null, 60_000.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nulo");
    }

    @Test
    void debeFallarConActivoVacio() {
        assertThatThrownBy(() -> new TickEvent("", 100.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConPrecioCero() {
        assertThatThrownBy(() -> new TickEvent("ETH", 0.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConPrecioNegativo() {
        assertThatThrownBy(() -> new TickEvent("ETH", -50.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConTimestampNulo() {
        assertThatThrownBy(() -> new TickEvent("GOOGL", 170.0, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("timestamp");
    }
}
