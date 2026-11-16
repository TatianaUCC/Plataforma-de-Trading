package com.trading.emailworker.adapter.in;

import com.rabbitmq.client.Channel;
import com.trading.emailworker.application.EmailNotificationService;
import com.trading.emailworker.config.RabbitMQConfig;
import com.trading.emailworker.domain.model.SignalNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adaptador de entrada: consume mensajes de la Work Queue 'email_queue'.
 *
 * ACK manual: el mensaje se confirma solo si el procesamiento termina sin error.
 * Si el servicio lanza excepción → basicNack con requeue=true → reintento.
 * Esto evita confirmar mensajes que no fueron procesados correctamente.
 */
@Component
public class EmailTaskListener {

    private static final Logger log = LoggerFactory.getLogger(EmailTaskListener.class);

    private final EmailNotificationService notificationService;

    public EmailTaskListener(EmailNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @RabbitListener(
        queues = RabbitMQConfig.QUEUE_EMAIL,
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void onMessage(SignalNotification notification,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            notificationService.procesar(notification);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error procesando notificación de email para asset={} — reencolando",
                notification.asset(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
