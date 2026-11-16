package com.trading.emailworker.adapter.out;

import com.trading.emailworker.domain.model.SignalNotification;
import com.trading.emailworker.domain.port.out.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulación de envío de email mediante log.
 * No hay proveedor SMTP configurado — el enunciado solo requiere simular el envío.
 *
 * Log exacto requerido por el enunciado:
 *   "ENVIANDO EMAIL: ¡Momento de Invertir! Señal de COMPRA para BTC"
 */
@Component
public class LogEmailSender implements EmailSenderPort {

    private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

    @Override
    public void enviar(SignalNotification notification) {
        log.info("ENVIANDO EMAIL: ¡Momento de Invertir! Señal de COMPRA para {}",
            notification.asset());
    }
}
