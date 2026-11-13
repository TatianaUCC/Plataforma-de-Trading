package com.trading.analytics.domain.model;

/**
 * Value Object inmutable que identifica un activo financiero.
 * Normaliza el código a mayúsculas con validación Fail-Fast.
 */
public record AssetCode(String code) {

    public AssetCode {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código del activo no puede ser nulo ni vacío");
        }
        code = code.trim().toUpperCase();
    }

    @Override
    public String toString() {
        return code;
    }
}
