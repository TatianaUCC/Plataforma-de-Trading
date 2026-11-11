package com.trading.bridge.adapter.in;

import com.rabbitmq.client.Channel;
import com.trading.bridge.application.BridgeService;
import com.trading.bridge.config.RabbitMQConfig;
import com.trading.bridge.domain.model.TickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Adaptador de entrada: consume ticks de RabbitMQ con ACK MANUAL.
 *
 * Flujo de confirmación:
 *   1. Se recibe el tick de RabbitMQ (no confirmado aún).
 *   2. Se llama a bridgeService.transferir(tick) — bloquea hasta que Kafka confirma.
 *   3. Si Kafka confirma → channel.basicAck → mensaje eliminado de la cola.
 *   4. Si Kafka falla  → channel.basicNack(requeue=true) → mensaje vuelve a la cola.
 *
 * Esto garantiza que ningún tick se pierda silenciosamente.
 */
@Component
public class BridgeTickListener {

    private static final Logger log = LoggerFactory.getLogger(BridgeTickListener.class);

    private final BridgeService bridgeService;

    public BridgeTickListener(BridgeService bridgeService) {
        this.bridgeService = bridgeService;
    }

    @RabbitListener(
        queues = RabbitMQConfig.QUEUE_BRIDGE_TICKS,
        containerFactory = "rabbitListenerContainerFactory"
    )
    public void onTick(TickEvent tick,
                       Channel channel,
                       @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        try {
            bridgeService.transferir(tick);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("Error transfiriendo tick {} a Kafka — reencolando", tick.asset(), e);
            // requeue=true: el mensaje vuelve a la cabeza de la cola para reintento
            channel.basicNack(deliveryTag, false, true);
        }
    }
}
