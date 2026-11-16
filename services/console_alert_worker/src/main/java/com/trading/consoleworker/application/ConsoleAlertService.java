package com.trading.consoleworker.application;

import com.trading.consoleworker.domain.model.AlertNotification;
import com.trading.consoleworker.domain.port.out.AlertPrinterPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación del console_alert_worker.
 * Recibe la alerta del listener RabbitMQ y la delega al puerto de impresión.
 * Si la impresión falla, propaga la excepción para que el listener haga NACK.
 */
@Service
public class ConsoleAlertService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleAlertService.class);

    private final AlertPrinterPort alertPrinter;

    public ConsoleAlertService(AlertPrinterPort alertPrinter) {
        this.alertPrinter = alertPrinter;
    }

    public void procesar(AlertNotification notification) {
        log.debug("Procesando alerta de consola: asset={} signal={}",
            notification.asset(), notification.signal());
        alertPrinter.imprimir(notification);
    }
}
