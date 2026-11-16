package com.trading.investdashboard.adapter.in;

import com.trading.investdashboard.application.SignalBroadcastService;
import com.trading.investdashboard.domain.model.SignalMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalConsumerTest {

    @Mock
    private SignalBroadcastService broadcastService;

    private SignalConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SignalConsumer(broadcastService);
    }

    @Test
    void onSignalDelegaAlServicio() {
        SignalMessage signal = new SignalMessage("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        consumer.onSignal(signal);

        verify(broadcastService).procesar(signal);
    }

    @Test
    void onSignalProcesaMultiplesSeñales() {
        SignalMessage s1 = new SignalMessage("BTC",  "BUY", "Momentum Crossover", 60000.0, 43000.0);
        SignalMessage s2 = new SignalMessage("AMZN", "BUY", "Momentum Crossover",   185.0,   160.0);

        consumer.onSignal(s1);
        consumer.onSignal(s2);

        verify(broadcastService).procesar(s1);
        verify(broadcastService).procesar(s2);
    }
}
