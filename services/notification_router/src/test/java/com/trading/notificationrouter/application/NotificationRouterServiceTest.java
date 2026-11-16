package com.trading.notificationrouter.application;

import com.trading.notificationrouter.domain.model.SignalEvent;
import com.trading.notificationrouter.domain.port.out.NotificationLogRepositoryPort;
import com.trading.notificationrouter.domain.port.out.NotificationPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationRouterServiceTest {

    @Mock
    private NotificationPublisherPort publisher;
    @Mock
    private NotificationLogRepositoryPort logRepository;

    private NotificationRouterService service;

    @BeforeEach
    void setUp() {
        service = new NotificationRouterService(publisher, logRepository);
    }

    @Test
    void enrutarPublicaEnRabbitMQ() {
        // Arrange
        SignalEvent signal = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        // Act
        service.enrutar(signal);

        // Assert
        verify(publisher).publicar(signal);
    }

    @Test
    void enrutarRegistraDespachoEmailYConsole() {
        // Arrange
        SignalEvent signal = new SignalEvent("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);

        // Act
        service.enrutar(signal);

        // Assert
        verify(logRepository).registrarDespacho(signal, "EMAIL");
        verify(logRepository).registrarDespacho(signal, "CONSOLE");
        verifyNoMoreInteractions(logRepository);
    }

    @Test
    void enrutarPublicaPrimeroYLuegoPersiste() {
        // Arrange
        SignalEvent signal = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        var order = inOrder(publisher, logRepository);

        // Act
        service.enrutar(signal);

        // Assert — el orden importa: RabbitMQ antes que BD
        order.verify(publisher).publicar(signal);
        order.verify(logRepository).registrarDespacho(any(), eq("EMAIL"));
        order.verify(logRepository).registrarDespacho(any(), eq("CONSOLE"));
    }

    @Test
    void falloRabbitMQPropagaExcepcion() {
        // Arrange
        SignalEvent signal = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        doThrow(new RuntimeException("RabbitMQ caído")).when(publisher).publicar(signal);

        // Act & Assert — fallo en RabbitMQ se propaga (Kafka no hace ACK)
        assertThatThrownBy(() -> service.enrutar(signal))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("RabbitMQ caído");

        // No debe persistir si RabbitMQ falló
        verifyNoInteractions(logRepository);
    }

    @Test
    void falloPersistenciaNoDetieneElFlujo() {
        // Arrange
        SignalEvent signal = new SignalEvent("GOOGL", "BUY", "Momentum Crossover", 170.0, 150.0);
        doThrow(new RuntimeException("BD caída"))
            .when(logRepository).registrarDespacho(any(), any());

        // Act — no debe lanzar excepción aunque la BD falle
        service.enrutar(signal);

        // Assert — RabbitMQ sí fue publicado
        verify(publisher).publicar(signal);
    }
}
