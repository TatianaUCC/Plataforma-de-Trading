package com.trading.notificationrouter.adapter.out;

import com.trading.notificationrouter.config.RabbitMQConfig;
import com.trading.notificationrouter.domain.model.SignalEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RabbitMQNotificationPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQNotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitMQNotificationPublisher(rabbitTemplate);
    }

    @Test
    void publicarEnviaAEmailQueue() {
        // Arrange
        SignalEvent signal = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        // Act
        publisher.publicar(signal);

        // Assert — Work Queue: exchange vacío, routing key = nombre de cola
        verify(rabbitTemplate).convertAndSend(eq(""), eq(RabbitMQConfig.QUEUE_EMAIL), eq(signal));
    }

    @Test
    void publicarEnviaAConsoleAlertsFanout() {
        // Arrange
        SignalEvent signal = new SignalEvent("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);

        // Act
        publisher.publicar(signal);

        // Assert — Fanout: exchange = console_alerts, routing key ignorada ("")
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE_CONSOLE_ALERTS), eq(""), eq(signal));
    }

    @Test
    void publicarRealizaDosEnvios() {
        // Arrange
        SignalEvent signal = new SignalEvent("AMZN", "BUY", "Momentum Crossover", 185.0, 160.0);

        // Act
        publisher.publicar(signal);

        // Assert — exactamente 2 invocaciones (email_queue + console_alerts)
        verify(rabbitTemplate, times(2))
            .convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void publicarEnviaElMismoPayloadAAmbosdestinos() {
        // Arrange
        SignalEvent signal = new SignalEvent("EURUSD", "BUY", "Momentum Crossover", 1.08, 1.0);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);

        // Act
        publisher.publicar(signal);

        // Assert
        verify(rabbitTemplate, times(2))
            .convertAndSend(anyString(), anyString(), payloadCaptor.capture());
        payloadCaptor.getAllValues().forEach(p ->
            assertThat(p).isEqualTo(signal)
        );
    }
}
