package com.trading.marketfeed.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Value Object inmutable que representa el código de un activo financiero.
 * Validación Fail-Fast en el constructor compacto.
 *
 * @JsonValue  — serializa el record directamente como string "BTC" (no como {"code":"BTC"})
 * @JsonCreator — permite deserializarlo desde un string "BTC"
 */
public record AssetCode(String code) {

    @JsonCreator
    public AssetCode {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código del activo no puede ser nulo ni vacío");
        }
        code = code.trim().toUpperCase();
    }

    @JsonValue
    @Override
    public String toString() {
        return code;
    }
}
