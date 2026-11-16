package com.trading.investdashboard.application;

import com.trading.investdashboard.domain.model.SignalMessage;
import com.trading.investdashboard.domain.port.out.SignalBroadcastPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalBroadcastServiceTest {

    @Mock
    private SignalBroadcastPort broadcastPort;

    private SignalBroadcastService service;

    @BeforeEach
    void setUp() {
        service = new SignalBroadcastService(broadcastPort);
    }

    @Test
    void procesarDelegaAlPuerto() {
        SignalMessage signal = new SignalMessage("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        service.procesar(signal);
        verify(broadcastPort).broadcast(signal);
    }

    @Test
    void procesarVariasSeñales() {
        SignalMessage s1 = new SignalMessage("BTC",  "BUY", "Momentum Crossover", 60000.0, 43000.0);
        SignalMessage s2 = new SignalMessage("ETH",  "BUY", "Momentum Crossover",  3000.0,  2500.0);
        SignalMessage s3 = new SignalMessage("AMZN", "BUY", "Momentum Crossover",   185.0,   160.0);

        service.procesar(s1);
        service.procesar(s2);
        service.procesar(s3);

        verify(broadcastPort).broadcast(s1);
        verify(broadcastPort).broadcast(s2);
        verify(broadcastPort).broadcast(s3);
        verifyNoMoreInteractions(broadcastPort);
    }
}
