package com.trading.consoleworker.adapter.out;

import com.trading.consoleworker.domain.model.AlertNotification;
import com.trading.consoleworker.domain.port.out.AlertPrinterPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Imprime la alerta en consola con el formato exacto del enunciado:
 *   "*** ALERTA DE SISTEMA: SEÑAL DE COMPRA EN BTC ***"
 */
@Component
public class ConsoleAlertPrinter implements AlertPrinterPort {

    private static final Logger log = LoggerFactory.getLogger(ConsoleAlertPrinter.class);

    @Override
    public void imprimir(AlertNotification notification) {
        log.info("*** ALERTA DE SISTEMA: SEÑAL DE COMPRA EN {} ***",
            notification.asset());
    }
}
