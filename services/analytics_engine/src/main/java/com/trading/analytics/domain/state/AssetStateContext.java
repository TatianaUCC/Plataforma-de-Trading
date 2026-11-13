package com.trading.analytics.domain.state;

/**
 * Contrato que el AssetTracker expone a los estados para que puedan
 * efectuar transiciones sin conocer la implementación completa del tracker.
 */
public interface AssetStateContext {

    void cambiarEstado(AssetState nuevoEstado);

    String getAssetCode();
}
