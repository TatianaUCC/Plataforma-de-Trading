package com.trading.bridge.application;

import com.trading.bridge.domain.model.TickEvent;
import com.trading.bridge.domain.port.out.TickKafkaPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BridgeServiceTest {

    @Mock
    private TickKafkaPublisherPort kafkaPublisher;

    private BridgeService bridgeService;

    @BeforeEach
    void setUp() {
        bridgeService = new BridgeService(kafkaPublisher);
    }

    @Test
    void debeDelegarAlPuertoKafka() {
        // Arrange
        TickEvent tick = new TickEvent("BTC", 60_000.0, Instant.now());

        // Act
        bridgeService.transferir(tick);

        // Assert
        verify(kafkaPublisher, times(1)).publicar(tick);
    }

    @Test
    void debePropagarsExcepcionCuandoKafkaFalla() {
        // Arrange
        TickEvent tick = new TickEvent("ETH", 3_000.0, Instant.now());
        doThrow(new RuntimeException("Kafka no disponible"))
            .when(kafkaPublisher).publicar(tick);

        // Act & Assert
        assertThatThrownBy(() -> bridgeService.transferir(tick))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Kafka no disponible");

        verify(kafkaPublisher).publicar(tick);
    }

    @Test
    void debeTransferirCadaActivoDeFormaSeparada() {
        // Arrange
        TickEvent btc    = new TickEvent("BTC",    60_000.0, Instant.now());
        TickEvent eth    = new TickEvent("ETH",     3_000.0, Instant.now());
        TickEvent eurusd = new TickEvent("EURUSD",     1.08, Instant.now());

        // Act
        bridgeService.transferir(btc);
        bridgeService.transferir(eth);
        bridgeService.transferir(eurusd);

        // Assert
        verify(kafkaPublisher).publicar(btc);
        verify(kafkaPublisher).publicar(eth);
        verify(kafkaPublisher).publicar(eurusd);
        verifyNoMoreInteractions(kafkaPublisher);
    }
}
