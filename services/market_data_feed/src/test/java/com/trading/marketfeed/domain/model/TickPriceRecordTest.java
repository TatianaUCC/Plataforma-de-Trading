package com.trading.marketfeed.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TickPriceRecordTest {

    private final AssetCode btc = new AssetCode("BTC");

    @Test
    void debeCrearseConDatosValidos() {
        Instant now = Instant.now();
        TickPriceRecord tick = new TickPriceRecord(btc, 60_000.0, now);

        assertThat(tick.assetCode()).isEqualTo(btc);
        assertThat(tick.price()).isEqualTo(60_000.0);
        assertThat(tick.timestamp()).isEqualTo(now);
    }

    @Test
    void debeFallarConPrecioCero() {
        assertThatThrownBy(() -> new TickPriceRecord(btc, 0.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConPrecioNegativo() {
        assertThatThrownBy(() -> new TickPriceRecord(btc, -1.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("mayor que cero");
    }

    @Test
    void debeFallarConAssetCodeNulo() {
        assertThatThrownBy(() -> new TickPriceRecord(null, 100.0, Instant.now()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConTimestampNulo() {
        assertThatThrownBy(() -> new TickPriceRecord(btc, 100.0, null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
