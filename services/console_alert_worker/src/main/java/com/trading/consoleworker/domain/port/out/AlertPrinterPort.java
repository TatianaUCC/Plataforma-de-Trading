package com.trading.consoleworker.domain.port.out;

import com.trading.consoleworker.domain.model.AlertNotification;

/**
 * Puerto de salida: desacopla el dominio del mecanismo concreto de impresión.
 */
public interface AlertPrinterPort {

    void imprimir(AlertNotification notification);
}
