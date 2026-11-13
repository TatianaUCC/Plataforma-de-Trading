package com.trading.analytics.application;

import com.trading.analytics.domain.model.SignalPayload;
import com.trading.analytics.domain.model.TickRecord;
import com.trading.analytics.domain.port.out.SignalPublisherPort;
import com.trading.analytics.domain.strategy.SimpleMovingAverageStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private SignalPublisherPort signalPublisher;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(new SimpleMovingAverageStrategy(), signalPublisher);
    }

    @Test
    void procesarTickConMenosDe5NoPublicaSenal() {
        TickRecord tick = new TickRecord("BTC", 60_000.0, Instant.now());
        service.procesarTick(tick);
        service.procesarTick(new TickRecord("BTC", 61_000.0, Instant.now()));
        service.procesarTick(new TickRecord("BTC", 59_000.0, Instant.now()));

        verifyNoInteractions(signalPublisher);
    }

    @Test
    void procesarTickSuperaUmbralPublicaSenal() {
        // 5 ticks planos seguidos de uno alto que desplaza
        for (int i = 0; i < 5; i++) {
            service.procesarTick(new TickRecord("BTC", 100.0, Instant.now()));
        }
        service.procesarTick(new TickRecord("BTC", 200.0, Instant.now()));

        ArgumentCaptor<SignalPayload> captor = ArgumentCaptor.forClass(SignalPayload.class);
        verify(signalPublisher).publicar(captor.capture());

        SignalPayload signal = captor.getValue();
        assertThat(signal.asset()).isEqualTo("BTC");
        assertThat(signal.signal()).isEqualTo("BUY");
        assertThat(signal.reason()).isEqualTo("Momentum Crossover");
    }

    @Test
    void procesarTickSinUmbralNoPublica() {
        for (int i = 0; i < 5; i++) {
            service.procesarTick(new TickRecord("ETH", 3000.0, Instant.now()));
        }

        verifyNoInteractions(signalPublisher);
    }

    @Test
    void mantieneTrackersIndependientesPorActivo() {
        // BTC con 5 ticks planos
        for (int i = 0; i < 5; i++) {
            service.procesarTick(new TickRecord("BTC", 100.0, Instant.now()));
        }
        // ETH con 2 ticks
        service.procesarTick(new TickRecord("ETH", 3000.0, Instant.now()));
        service.procesarTick(new TickRecord("ETH", 3100.0, Instant.now()));

        // BTC tiene 5 ticks, ETH tiene 2
        assertThat(service.getEstado("BTC")).isEqualTo("NORMAL");
        assertThat(service.getEstado("ETH")).isEqualTo("NORMAL");
    }

    @Test
    void getEstadoAssetCodeNormalizaAMayusculas() {
        service.procesarTick(new TickRecord("btc", 100.0, Instant.now()));
        assertThat(service.getEstado("btc")).isEqualTo("NORMAL");
    }
}
