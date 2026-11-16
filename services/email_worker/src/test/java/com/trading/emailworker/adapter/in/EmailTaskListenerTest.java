package com.trading.emailworker.adapter.in;

import com.rabbitmq.client.Channel;
import com.trading.emailworker.application.EmailNotificationService;
import com.trading.emailworker.domain.model.SignalNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailTaskListenerTest {

    @Mock
    private EmailNotificationService notificationService;

    @Mock
    private Channel channel;

    private EmailTaskListener listener;

    @BeforeEach
    void setUp() {
        listener = new EmailTaskListener(notificationService);
    }

    @Test
    void debeHacerAckCuandoElProcesadoEsExitoso() throws IOException {
        // Arrange
        SignalNotification notification = new SignalNotification("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        long deliveryTag = 1L;

        // Act
        listener.onMessage(notification, channel, deliveryTag);

        // Assert
        verify(notificationService).procesar(notification);
        verify(channel).basicAck(deliveryTag, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void debeHacerNackConRequeueCuandoElProcesadoFalla() throws IOException {
        // Arrange
        SignalNotification notification = new SignalNotification("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);
        long deliveryTag = 2L;
        doThrow(new RuntimeException("Fallo simulado")).when(notificationService).procesar(notification);

        // Act
        listener.onMessage(notification, channel, deliveryTag);

        // Assert
        verify(notificationService).procesar(notification);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verify(channel).basicNack(deliveryTag, false, true);
    }

    @Test
    void debeHacerAckIndependientePorCadaMensaje() throws IOException {
        // Arrange
        SignalNotification n1 = new SignalNotification("BTC",  "BUY", "Momentum Crossover", 60000.0, 43000.0);
        SignalNotification n2 = new SignalNotification("AMZN", "BUY", "Momentum Crossover",   185.0,   160.0);

        // Act
        listener.onMessage(n1, channel, 10L);
        listener.onMessage(n2, channel, 11L);

        // Assert — cada mensaje confirmado individualmente
        verify(channel).basicAck(10L, false);
        verify(channel).basicAck(11L, false);
    }

    @Test
    void debeProcesarNotificacionConPrecioPequeno() throws IOException {
        // Arrange — EURUSD con precio < 2
        SignalNotification notification = new SignalNotification("EURUSD", "BUY", "Momentum Crossover", 1.08, 1.0);
        long deliveryTag = 5L;

        // Act
        listener.onMessage(notification, channel, deliveryTag);

        // Assert
        verify(notificationService).procesar(notification);
        verify(channel).basicAck(deliveryTag, false);
    }
}
