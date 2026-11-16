package com.trading.notificationrouter.adapter.in;

import com.trading.notificationrouter.application.NotificationRouterService;
import com.trading.notificationrouter.domain.model.SignalEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalKafkaConsumerTest {

    @Mock
    private NotificationRouterService routerService;

    private SignalKafkaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new SignalKafkaConsumer(routerService);
    }

    @Test
    void onSignalDelegaAlServicio() {
        // Arrange
        SignalEvent signal = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        // Act
        consumer.onSignal(signal);

        // Assert
        verify(routerService).enrutar(signal);
    }

    @Test
    void onSignalPropagaExcepcionParaQueKafkaNoHagaAck() {
        // Arrange
        SignalEvent signal = new SignalEvent("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);
        doThrow(new RuntimeException("RabbitMQ no disponible"))
            .when(routerService).enrutar(signal);

        // Act & Assert — la excepción debe propagarse para que Kafka reintente
        assertThatThrownBy(() -> consumer.onSignal(signal))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("RabbitMQ no disponible");
    }
}
