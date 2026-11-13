package com.trading.analytics.domain.strategy;

import java.util.List;

/**
 * Puerto Strategy: define el contrato para calcular indicadores técnicos.
 * El motor analítico depende de esta interfaz, no de implementaciones concretas.
 */
public interface IndicatorStrategy {

    /**
     * Calcula el indicador a partir del historial de precios.
     * @param prices lista de precios más recientes (hasta la ventana configurada)
     * @return valor calculado del indicador
     */
    double calcular(List<Double> prices);

    /**
     * Evalúa si el precio actual supera el umbral definido sobre el indicador.
     * @param precioActual precio del tick en evaluación
     * @param valorIndicador resultado de calcular()
     * @return true si se cumple la condición de momentum
     */
    boolean superaUmbral(double precioActual, double valorIndicador);

    /**
     * Número de ticks mínimos necesarios para que el cálculo sea válido.
     */
    int ventanaMinima();
}
