package com.trading.investdashboard.adapter.out;

import com.trading.investdashboard.domain.model.SignalMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebSocketSignalBroadcasterTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketSignalBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new WebSocketSignalBroadcaster(messagingTemplate);
    }

    @Test
    void broadcastEnviaAlTopicCorrecto() {
        SignalMessage signal = new SignalMessage("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        broadcaster.broadcast(signal);

        verify(messagingTemplate).convertAndSend(eq("/topic/signals"), eq(signal));
    }

    @Test
    void broadcastEnviaElPayloadCompleto() {
        SignalMessage signal = new SignalMessage("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);
        ArgumentCaptor<SignalMessage> captor = ArgumentCaptor.forClass(SignalMessage.class);

        broadcaster.broadcast(signal);

        verify(messagingTemplate).convertAndSend(anyString(), captor.capture());
        assertThat(captor.getValue().asset()).isEqualTo("ETH");
        assertThat(captor.getValue().signal()).isEqualTo("BUY");
        assertThat(captor.getValue().price()).isEqualTo(3000.0);
    }

    @Test
    void broadcastMultiplesSeñales() {
        SignalMessage s1 = new SignalMessage("BTC",    60000.0 > 0 ? "BUY" : "SELL", "reason", 60000.0, 43000.0);
        SignalMessage s2 = new SignalMessage("EURUSD", "BUY", "reason", 1.08, 1.0);

        broadcaster.broadcast(s1);
        broadcaster.broadcast(s2);

        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/signals"), any(SignalMessage.class));
    }
}
