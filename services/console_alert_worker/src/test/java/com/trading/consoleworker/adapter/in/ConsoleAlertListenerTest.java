package com.trading.consoleworker.adapter.in;

import com.rabbitmq.client.Channel;
import com.trading.consoleworker.application.ConsoleAlertService;
import com.trading.consoleworker.domain.model.AlertNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleAlertListenerTest {

    @Mock
    private ConsoleAlertService alertService;

    @Mock
    private Channel channel;

    private ConsoleAlertListener listener;

    @BeforeEach
    void setUp() {
        listener = new ConsoleAlertListener(alertService);
    }

    @Test
    void debeHacerAckCuandoElProcesadoEsExitoso() throws IOException {
        // Arrange
        AlertNotification notification = new AlertNotification("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        long deliveryTag = 1L;

        // Act
        listener.onAlert(notification, channel, deliveryTag);

        // Assert
        verify(alertService).procesar(notification);
        verify(channel).basicAck(deliveryTag, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void debeHacerNackConRequeueCuandoElProcesadoFalla() throws IOException {
        // Arrange
        AlertNotification notification = new AlertNotification("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);
        long deliveryTag = 2L;
        doThrow(new RuntimeException("Fallo simulado")).when(alertService).procesar(notification);

        // Act
        listener.onAlert(notification, channel, deliveryTag);

        // Assert
        verify(alertService).procesar(notification);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel).basicNack(deliveryTag, false, true);
    }

    @Test
    void debeHacerAckIndependientePorCadaMensaje() throws IOException {
        // Arrange
        AlertNotification n1 = new AlertNotification("BTC",  "BUY", "Momentum Crossover", 60000.0, 43000.0);
        AlertNotification n2 = new AlertNotification("AMZN", "BUY", "Momentum Crossover",   185.0,   160.0);

        // Act
        listener.onAlert(n1, channel, 10L);
        listener.onAlert(n2, channel, 11L);

        // Assert
        verify(channel).basicAck(10L, false);
        verify(channel).basicAck(11L, false);
    }

    @Test
    void debeProcesarAlertaDeActivoForex() throws IOException {
        // Arrange
        AlertNotification notification = new AlertNotification("EURUSD", "BUY", "Momentum Crossover", 1.08, 1.0);
        long deliveryTag = 5L;

        // Act
        listener.onAlert(notification, channel, deliveryTag);

        // Assert
        verify(alertService).procesar(notification);
        verify(channel).basicAck(deliveryTag, false);
    }
}
