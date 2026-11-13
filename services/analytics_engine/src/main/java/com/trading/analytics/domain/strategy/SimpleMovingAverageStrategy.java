package com.trading.analytics.domain.strategy;

import java.util.List;

/**
 * Calcula la Media Móvil Simple (SMA) sobre los últimos 5 ticks.
 * Genera señal cuando: precio_actual > SMA * 1.05 (umbral 5%).
 *
 * Decisiones D-05 y D-06: ventana=5, umbral=5%. No se modifica.
 */
public class SimpleMovingAverageStrategy implements IndicatorStrategy {

    private static final int VENTANA     = 5;
    private static final double UMBRAL   = 1.05;

    @Override
    public double calcular(List<Double> prices) {
        if (prices == null || prices.size() < VENTANA) {
            throw new IllegalArgumentException(
                "Se necesitan al menos " + VENTANA + " precios para calcular la SMA");
        }
        List<Double> ventana = prices.subList(prices.size() - VENTANA, prices.size());
        return ventana.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
    }

    @Override
    public boolean superaUmbral(double precioActual, double valorIndicador) {
        return precioActual > valorIndicador * UMBRAL;
    }

    @Override
    public int ventanaMinima() {
        return VENTANA;
    }
}
