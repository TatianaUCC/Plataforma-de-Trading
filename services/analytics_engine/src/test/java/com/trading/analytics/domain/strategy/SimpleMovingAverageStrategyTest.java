package com.trading.analytics.domain.strategy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleMovingAverageStrategyTest {

    private final SimpleMovingAverageStrategy strategy = new SimpleMovingAverageStrategy();

    @Test
    void calcularPromedioDe5Precios() {
        List<Double> precios = List.of(100.0, 200.0, 300.0, 400.0, 500.0);
        assertThat(strategy.calcular(precios)).isEqualTo(300.0);
    }

    @Test
    void ventanaMinimaEs5() {
        assertThat(strategy.ventanaMinima()).isEqualTo(5);
    }

    @Test
    void debeFallarConMenosDe5Precios() {
        assertThatThrownBy(() -> strategy.calcular(List.of(100.0, 200.0)))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("5");
    }

    @Test
    void debeFallarConListaNula() {
        assertThatThrownBy(() -> strategy.calcular(null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void superaUmbralTrueCuandoPrecioSupera5PorCiento() {
        double sma = 100.0;
        double precio = 106.0;
        assertThat(strategy.superaUmbral(precio, sma)).isTrue();
    }

    @Test
    void superaUmbralFalseCuandoPrecioNoSupera5PorCiento() {
        double sma = 100.0;
        double precio = 104.9;
        assertThat(strategy.superaUmbral(precio, sma)).isFalse();
    }

    @Test
    void superaUmbralEnElLimiteExactoEsFalse() {
        double sma = 100.0;
        double precio = 105.0;
        // 100 * 1.05 = 105 → estrictamente mayor es falso
        assertThat(strategy.superaUmbral(precio, sma)).isFalse();
    }
}
