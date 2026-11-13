package com.trading.analytics.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetCodeTest {

    @Test
    void debeCrearseConCodigoValido() {
        AssetCode code = new AssetCode("btc");
        assertThat(code.code()).isEqualTo("BTC");
    }

    @Test
    void debeNormalizarAMayusculas() {
        assertThat(new AssetCode("eth").code()).isEqualTo("ETH");
    }

    @Test
    void debeFallarConCodigoNulo() {
        assertThatThrownBy(() -> new AssetCode(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConCodigoVacio() {
        assertThatThrownBy(() -> new AssetCode(""))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
