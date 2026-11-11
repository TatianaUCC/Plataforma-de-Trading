package com.trading.livedashboard.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TickMessageTest {

    @Test
    void debeCrearseConDatosValidos() {
        Instant now = Instant.now();
        TickMessage tick = new TickMessage("BTC", 60_000.0, now);

        assertThat(tick.asset()).isEqualTo("BTC");
        assertThat(tick.price()).isEqualTo(60_000.0);
        assertThat(tick.timestamp()).isEqualTo(now);
    }

    @Test
    void debeFallarConActivoNulo() {
        assertThatThrownBy(() -> new TickMessage(null, 60_000.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nulo");
    }

    @Test
    void debeFallarConActivoVacio() {
        assertThatThrownBy(() -> new TickMessage("", 60_000.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConActivoEnBlanco() {
        assertThatThrownBy(() -> new TickMessage("   ", 60_000.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConPrecioCero() {
        assertThatThrownBy(() -> new TickMessage("ETH", 0.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConPrecioNegativo() {
        assertThatThrownBy(() -> new TickMessage("ETH", -1.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConTimestampNulo() {
        assertThatThrownBy(() -> new TickMessage("BTC", 60_000.0, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("timestamp");
    }
}
