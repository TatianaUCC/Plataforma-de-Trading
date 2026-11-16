package com.trading.emailworker.application;

import com.trading.emailworker.domain.model.SignalNotification;
import com.trading.emailworker.domain.port.out.EmailSenderPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private EmailSenderPort emailSender;

    private EmailNotificationService service;

    @BeforeEach
    void setUp() {
        service = new EmailNotificationService(emailSender);
    }

    @Test
    void procesarDelegaAlPuertoDeEnvio() {
        // Arrange
        SignalNotification notification = new SignalNotification("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        // Act
        service.procesar(notification);

        // Assert
        verify(emailSender).enviar(notification);
    }

    @Test
    void procesarPropagaExcepcionCuandoElEnvioFalla() {
        // Arrange
        SignalNotification notification = new SignalNotification("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);
        doThrow(new RuntimeException("Error de envío")).when(emailSender).enviar(notification);

        // Act & Assert — la excepción se propaga para que el listener haga NACK
        assertThatThrownBy(() -> service.procesar(notification))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error de envío");

        verify(emailSender).enviar(notification);
    }

    @Test
    void procesarVariasNotificacionesDeFormaSeparada() {
        // Arrange
        SignalNotification n1 = new SignalNotification("BTC",   "BUY", "Momentum Crossover", 60000.0, 43000.0);
        SignalNotification n2 = new SignalNotification("ETH",   "BUY", "Momentum Crossover",  3000.0,  2500.0);
        SignalNotification n3 = new SignalNotification("GOOGL", "BUY", "Momentum Crossover",   170.0,   150.0);

        // Act
        service.procesar(n1);
        service.procesar(n2);
        service.procesar(n3);

        // Assert
        verify(emailSender).enviar(n1);
        verify(emailSender).enviar(n2);
        verify(emailSender).enviar(n3);
        verifyNoMoreInteractions(emailSender);
    }
}
