package com.trading.marketfeed.adapter.out;

import com.trading.marketfeed.config.RabbitMQConfig;
import com.trading.marketfeed.domain.model.TickPriceRecord;
import com.trading.marketfeed.domain.port.out.TickPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Adaptador de salida: implementa TickPublisherPort usando Spring AMQP.
 * Construye el routing key según el tipo de activo y publica en el Topic Exchange.
 *
 * Routing keys:
 *   BTC, ETH          → ticks.crypto.<asset>
 *   EURUSD            → ticks.forex.<asset>
 *   GOOGL, AMZN       → ticks.stock.<asset>
 */
@Component
public class RabbitMQTickPublisher implements TickPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(RabbitMQTickPublisher.class);

    private static final Map<String, String> ASSET_TYPE = Map.of(
        "BTC",    "crypto",
        "ETH",    "crypto",
        "EURUSD", "forex",
        "GOOGL",  "stock",
        "AMZN",   "stock"
    );

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQTickPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publicar(TickPriceRecord tick) {
        String routingKey = buildRoutingKey(tick.assetCode().code());
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE_MARKET_TICKS,
            routingKey,
            tick
        );
        log.info("Publicado tick: routingKey={} price={}", routingKey, tick.price());
    }

    private String buildRoutingKey(String assetCode) {
        String type = ASSET_TYPE.getOrDefault(assetCode, "unknown");
        return "ticks." + type + "." + assetCode.toLowerCase();
    }
}
