package com.trading.marketfeed.service;

import com.trading.marketfeed.domain.model.AssetCode;
import com.trading.marketfeed.domain.model.TickPriceRecord;
import com.trading.marketfeed.domain.port.out.TickPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Genera ticks de precio simulados mediante Random Walk (±2%) para 5 activos
 * y los publica a través del puerto de salida cada segundo.
 */
@Service
public class PriceGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PriceGeneratorService.class);

    private static final double DELTA_MAX = 0.02;

    private final TickPublisherPort publisher;
    private final Map<AssetCode, Double> currentPrices;

    public PriceGeneratorService(TickPublisherPort publisher) {
        this.publisher = publisher;
        this.currentPrices = initialPrices();
    }

    @Scheduled(fixedRateString = "${market.tick.interval-ms:1000}")
    public void generarTicks() {
        currentPrices.forEach((assetCode, price) -> {
            double nuevoPrecio = calcularNuevoPrecio(price);
            currentPrices.put(assetCode, nuevoPrecio);

            TickPriceRecord tick = new TickPriceRecord(assetCode, nuevoPrecio, Instant.now());
            publisher.publicar(tick);
            log.debug("Tick generado: {} = {}", assetCode, nuevoPrecio);
        });
    }

    double calcularNuevoPrecio(double precioActual) {
        double delta = (Math.random() * 2 * DELTA_MAX) - DELTA_MAX;
        return precioActual * (1 + delta);
    }

    private Map<AssetCode, Double> initialPrices() {
        Map<AssetCode, Double> prices = new ConcurrentHashMap<>();
        prices.put(new AssetCode("BTC"),    60_000.0);
        prices.put(new AssetCode("ETH"),     3_000.0);
        prices.put(new AssetCode("EURUSD"),      1.08);
        prices.put(new AssetCode("GOOGL"),     170.0);
        prices.put(new AssetCode("AMZN"),      185.0);
        return prices;
    }
}
