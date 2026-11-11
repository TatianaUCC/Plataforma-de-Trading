package com.trading.livedashboard.adapter.in;

import com.trading.livedashboard.domain.model.TickMessage;
import com.trading.livedashboard.domain.port.out.TickBroadcastPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TickMessageListenerTest {

    @Mock
    private TickBroadcastPort broadcastPort;

    private TickMessageListener listener;

    @BeforeEach
    void setUp() {
        listener = new TickMessageListener(broadcastPort);
    }

    @Test
    void debeReenviarElTickAlBroadcastPort() {
        // Arrange
        TickMessage tick = new TickMessage("BTC", 60_000.0, Instant.now());

        // Act
        listener.onTick(tick);

        // Assert
        verify(broadcastPort, times(1)).broadcast(tick);
    }

    @Test
    void debeReenviarCadaTickRecibido() {
        // Arrange
        TickMessage tick1 = new TickMessage("BTC", 60_000.0, Instant.now());
        TickMessage tick2 = new TickMessage("ETH", 3_000.0, Instant.now());
        TickMessage tick3 = new TickMessage("GOOGL", 170.0, Instant.now());

        // Act
        listener.onTick(tick1);
        listener.onTick(tick2);
        listener.onTick(tick3);

        // Assert
        verify(broadcastPort).broadcast(tick1);
        verify(broadcastPort).broadcast(tick2);
        verify(broadcastPort).broadcast(tick3);
        verifyNoMoreInteractions(broadcastPort);
    }

    @Test
    void debeReenviarTickConActivoForex() {
        // Arrange
        TickMessage tick = new TickMessage("EURUSD", 1.08, Instant.now());

        // Act
        listener.onTick(tick);

        // Assert
        verify(broadcastPort).broadcast(tick);
    }

    @Test
    void debeReenviarTickConPrecioPequeno() {
        // Arrange — precio de EURUSD que es < 2
        TickMessage tick = new TickMessage("EURUSD", 1.0742, Instant.now());

        // Act
        listener.onTick(tick);

        // Assert
        verify(broadcastPort).broadcast(argThat(t ->
            t.asset().equals("EURUSD") && t.price() == 1.0742
        ));
    }
}
