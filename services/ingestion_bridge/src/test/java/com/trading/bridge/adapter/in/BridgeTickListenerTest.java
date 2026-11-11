package com.trading.bridge.adapter.in;

import com.rabbitmq.client.Channel;
import com.trading.bridge.application.BridgeService;
import com.trading.bridge.domain.model.TickEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BridgeTickListenerTest {

    @Mock
    private BridgeService bridgeService;

    @Mock
    private Channel channel;

    private BridgeTickListener listener;

    @BeforeEach
    void setUp() {
        listener = new BridgeTickListener(bridgeService);
    }

    @Test
    void debeAcknowledgeCuandoKafkaConfirma() throws IOException {
        // Arrange
        TickEvent tick = new TickEvent("BTC", 60_000.0, Instant.now());
        long deliveryTag = 42L;

        // Act
        listener.onTick(tick, channel, deliveryTag);

        // Assert
        verify(bridgeService).transferir(tick);
        verify(channel).basicAck(deliveryTag, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void debeNackConRequeueCuandoKafkaFalla() throws IOException {
        // Arrange
        TickEvent tick = new TickEvent("ETH", 3_000.0, Instant.now());
        long deliveryTag = 7L;
        doThrow(new RuntimeException("Kafka caído"))
            .when(bridgeService).transferir(tick);

        // Act
        listener.onTick(tick, channel, deliveryTag);

        // Assert
        verify(bridgeService).transferir(tick);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel).basicNack(deliveryTag, false, true);
    }

    @Test
    void debeAcknowledgeCadaMensajeDeFormaSeparada() throws IOException {
        // Arrange
        TickEvent tick1 = new TickEvent("GOOGL", 170.0, Instant.now());
        TickEvent tick2 = new TickEvent("AMZN",  185.0, Instant.now());

        // Act
        listener.onTick(tick1, channel, 1L);
        listener.onTick(tick2, channel, 2L);

        // Assert
        verify(bridgeService).transferir(tick1);
        verify(bridgeService).transferir(tick2);
        verify(channel).basicAck(1L, false);
        verify(channel).basicAck(2L, false);
    }

    @Test
    void debeNackSoloElMensajeFallido() throws IOException {
        // Arrange
        TickEvent tick = new TickEvent("EURUSD", 1.08, Instant.now());
        long deliveryTag = 99L;
        doThrow(new RuntimeException("Timeout Kafka"))
            .when(bridgeService).transferir(tick);

        // Act
        listener.onTick(tick, channel, deliveryTag);

        // Assert
        verify(channel).basicNack(eq(deliveryTag), eq(false), eq(true));
    }
}
