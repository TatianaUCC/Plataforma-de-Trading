package com.trading.consoleworker.adapter.in;

import com.rabbitmq.client.Channel;
import com.trading.consoleworker.application.ConsoleAlertService;
import com.trading.consoleworker.config.RabbitMQConfig;
import com.trading.consoleworker.domain.model.AlertNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adaptador de entrada: consume alertas del Fanout Exchange 'console_alerts'.
 *
 * La cola usada es 'console_alert_queue' (durable), declarada y ligada al Fanout
 * en RabbitMQConfig. Cada instancia del worker comparte esta cola o puede usar
 * su propia instancia dependiendo del despliegue.
 *
 * ACK manual: confirma solo tras procesamiento exitoso.
 * NACK con requeue=true ante cualquier fallo.
 */
@Component
public class ConsoleAlertListener {

    private static final Logger log = LoggerFactory.getLogger(ConsoleAlertListener.class);

    private final ConsoleAlertService alertService;

    public ConsoleAlertListener(ConsoleAlertService alertService) {
        this.alertService = alertService;
    }

    @RabbitListener(
        queues = RabbitMQConfig.QUEUE_CONSOLE_ALERTS,
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void onAlert(AlertNotification notification,
                        Channel channel,
                        @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            alertService.procesar(notification);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error procesando alerta para asset={} — reencolando",
                notification.asset(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
