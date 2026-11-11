package com.trading.livedashboard.adapter.out;

import com.trading.livedashboard.domain.model.TickMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketTickBroadcasterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketTickBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new WebSocketTickBroadcaster(messagingTemplate);
    }

    @Test
    void debeBroadcastearAlTopicCorrecto() {
        // Arrange
        TickMessage tick = new TickMessage("BTC", 60_000.0, Instant.now());

        // Act
        broadcaster.broadcast(tick);

        // Assert
        verify(messagingTemplate).convertAndSend(eq("/topic/ticks"), eq(tick));
    }

    @Test
    void debeBroadcastearElTickCompleto() {
        // Arrange
        Instant now = Instant.now();
        TickMessage tick = new TickMessage("ETH", 3_000.0, now);
        ArgumentCaptor<TickMessage> captor = ArgumentCaptor.forClass(TickMessage.class);

        // Act
        broadcaster.broadcast(tick);

        // Assert
        verify(messagingTemplate).convertAndSend(anyString(), captor.capture());
        TickMessage captured = captor.getValue();
        assertThat(captured.asset()).isEqualTo("ETH");
        assertThat(captured.price()).isEqualTo(3_000.0);
        assertThat(captured.timestamp()).isEqualTo(now);
    }

    @Test
    void debeBroadcastearMultiplesTicks() {
        // Arrange
        TickMessage t1 = new TickMessage("BTC",    60_000.0, Instant.now());
        TickMessage t2 = new TickMessage("EURUSD",     1.08, Instant.now());
        TickMessage t3 = new TickMessage("GOOGL",    170.0,  Instant.now());

        // Act
        broadcaster.broadcast(t1);
        broadcaster.broadcast(t2);
        broadcaster.broadcast(t3);

        // Assert
        verify(messagingTemplate, times(3)).convertAndSend(eq("/topic/ticks"), any(TickMessage.class));
    }

    @Test
    void debeBroadcastearUsandoSiempreElMismoTopic() {
        // Arrange
        TickMessage tick = new TickMessage("AMZN", 185.0, Instant.now());
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);

        // Act
        broadcaster.broadcast(tick);

        // Assert
        verify(messagingTemplate).convertAndSend(topicCaptor.capture(), any(TickMessage.class));
        assertThat(topicCaptor.getValue()).isEqualTo("/topic/ticks");
    }
}
