package com.trading.marketfeed.service;

import com.trading.marketfeed.domain.model.TickPriceRecord;
import com.trading.marketfeed.domain.port.out.TickPublisherPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceGeneratorServiceTest {

    @Mock
    private TickPublisherPort publisher;

    private PriceGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new PriceGeneratorService(publisher);
    }

    @Test
    void generarTicksDebePublicarCincoTicks() {
        service.generarTicks();
        verify(publisher, times(5)).publicar(any(TickPriceRecord.class));
    }

    @Test
    void generarTicksDebePublicarTicksConPrecioPositivo() {
        ArgumentCaptor<TickPriceRecord> captor = ArgumentCaptor.forClass(TickPriceRecord.class);

        service.generarTicks();

        verify(publisher, times(5)).publicar(captor.capture());
        captor.getAllValues().forEach(tick ->
            assertThat(tick.price()).isGreaterThan(0)
        );
    }

    @Test
    void generarTicksDebePublicarTicksConAssetCodeNoNulo() {
        ArgumentCaptor<TickPriceRecord> captor = ArgumentCaptor.forClass(TickPriceRecord.class);

        service.generarTicks();

        verify(publisher, times(5)).publicar(captor.capture());
        captor.getAllValues().forEach(tick ->
            assertThat(tick.assetCode()).isNotNull()
        );
    }

    @Test
    void calcularNuevoPrecioDebeEstarDentroDelRango() {
        double precioBase = 1000.0;

        for (int i = 0; i < 100; i++) {
            double nuevoPrecio = service.calcularNuevoPrecio(precioBase);
            assertThat(nuevoPrecio).isBetween(precioBase * 0.98, precioBase * 1.02);
            assertThat(nuevoPrecio).isGreaterThan(0);
        }
    }

    @Test
    void calcularNuevoPrecioConPrecioAltoDebeMantenerseProporcional() {
        double precioBTC = 60_000.0;
        double nuevoPrecio = service.calcularNuevoPrecio(precioBTC);
        assertThat(nuevoPrecio).isBetween(precioBTC * 0.98, precioBTC * 1.02);
    }
}
