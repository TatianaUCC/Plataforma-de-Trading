package com.trading.consoleworker.application;

import com.trading.consoleworker.domain.model.AlertNotification;
import com.trading.consoleworker.domain.port.out.AlertPrinterPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsoleAlertServiceTest {

    @Mock
    private AlertPrinterPort alertPrinter;

    private ConsoleAlertService service;

    @BeforeEach
    void setUp() {
        service = new ConsoleAlertService(alertPrinter);
    }

    @Test
    void procesarDelegaAlPuertoDeImpresion() {
        // Arrange
        AlertNotification notification = new AlertNotification("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        // Act
        service.procesar(notification);

        // Assert
        verify(alertPrinter).imprimir(notification);
    }

    @Test
    void procesarPropagaExcepcionCuandoLaImpresionFalla() {
        // Arrange
        AlertNotification notification = new AlertNotification("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);
        doThrow(new RuntimeException("Error de impresión")).when(alertPrinter).imprimir(notification);

        // Act & Assert
        assertThatThrownBy(() -> service.procesar(notification))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error de impresión");

        verify(alertPrinter).imprimir(notification);
    }

    @Test
    void procesarVariasAlertasDeFormaSeparada() {
        // Arrange
        AlertNotification n1 = new AlertNotification("BTC",   "BUY", "Momentum Crossover", 60000.0, 43000.0);
        AlertNotification n2 = new AlertNotification("ETH",   "BUY", "Momentum Crossover",  3000.0,  2500.0);
        AlertNotification n3 = new AlertNotification("EURUSD","BUY", "Momentum Crossover",     1.08,    1.0);

        // Act
        service.procesar(n1);
        service.procesar(n2);
        service.procesar(n3);

        // Assert
        verify(alertPrinter).imprimir(n1);
        verify(alertPrinter).imprimir(n2);
        verify(alertPrinter).imprimir(n3);
        verifyNoMoreInteractions(alertPrinter);
    }
}
