package com.trading.marketfeed.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AssetCodeTest {

    @Test
    void debeCrearseConCodigoValido() {
        AssetCode code = new AssetCode("BTC");
        assertThat(code.code()).isEqualTo("BTC");
    }

    @Test
    void debeNormalizarseAMayusculas() {
        AssetCode code = new AssetCode("eth");
        assertThat(code.code()).isEqualTo("ETH");
    }

    @Test
    void debeEliminarEspacios() {
        AssetCode code = new AssetCode("  GOOGL  ");
        assertThat(code.code()).isEqualTo("GOOGL");
    }

    @Test
    void debeFallarConCodigoNulo() {
        assertThatThrownBy(() -> new AssetCode(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nulo");
    }

    @Test
    void debeFallarConCodigoVacio() {
        assertThatThrownBy(() -> new AssetCode(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void debeFallarConCodigoEnBlanco() {
        assertThatThrownBy(() -> new AssetCode("   "))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
