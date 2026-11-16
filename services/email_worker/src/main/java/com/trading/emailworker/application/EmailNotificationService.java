package com.trading.emailworker.application;

import com.trading.emailworker.domain.model.SignalNotification;
import com.trading.emailworker.domain.port.out.EmailSenderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Servicio de aplicación del email_worker.
 * Recibe la notificación del listener RabbitMQ y la delega al puerto de envío.
 * Si el envío falla, propaga la excepción para que el listener haga NACK.
 */
@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final EmailSenderPort emailSender;

    public EmailNotificationService(EmailSenderPort emailSender) {
        this.emailSender = emailSender;
    }

    public void procesar(SignalNotification notification) {
        log.info("Procesando notificación de email: asset={} signal={}",
            notification.asset(), notification.signal());
        emailSender.enviar(notification);
    }
}
