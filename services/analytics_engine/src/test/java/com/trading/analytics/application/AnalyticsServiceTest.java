package com.trading.analytics.application;

import com.trading.analytics.domain.model.SignalPayload;
import com.trading.analytics.domain.model.TickRecord;
import com.trading.analytics.domain.port.out.SignalPublisherPort;
import com.trading.analytics.domain.port.out.SignalRepositoryPort;
import com.trading.analytics.domain.port.out.TickRecordRepositoryPort;
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
    @Mock
    private SignalRepositoryPort signalRepository;
    @Mock
    private TickRecordRepositoryPort tickRepository;

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(
            new SimpleMovingAverageStrategy(),
            signalPublisher,
            signalRepository,
            tickRepository
        );
    }

    @Test
    void procesarTickConMenosDe5NoPublicaSenal() {
        service.procesarTick(new TickRecord("BTC", 60_000.0, Instant.now()));
        service.procesarTick(new TickRecord("BTC", 61_000.0, Instant.now()));
        service.procesarTick(new TickRecord("BTC", 59_000.0, Instant.now()));

        verifyNoInteractions(signalPublisher);
    }

    @Test
    void procesarTickSuperaUmbralPublicaSenal() {
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
        for (int i = 0; i < 5; i++) {
            service.procesarTick(new TickRecord("BTC", 100.0, Instant.now()));
        }
        service.procesarTick(new TickRecord("ETH", 3000.0, Instant.now()));
        service.procesarTick(new TickRecord("ETH", 3100.0, Instant.now()));

        assertThat(service.getEstado("BTC")).isEqualTo("NORMAL");
        assertThat(service.getEstado("ETH")).isEqualTo("NORMAL");
    }

    @Test
    void getEstadoAssetCodeNormalizaAMayusculas() {
        service.procesarTick(new TickRecord("btc", 100.0, Instant.now()));
        assertThat(service.getEstado("btc")).isEqualTo("NORMAL");
    }

    // --- Pruebas de persistencia ---

    @Test
    void procesarTickPersisteCadaTickRecibido() {
        TickRecord tick = new TickRecord("BTC", 60_000.0, Instant.now());
        service.procesarTick(tick);

        verify(tickRepository).guardar(tick);
    }

    @Test
    void procesarTickConSenalPersisteTickYsenal() {
        for (int i = 0; i < 5; i++) {
            service.procesarTick(new TickRecord("BTC", 100.0, Instant.now()));
        }
        service.procesarTick(new TickRecord("BTC", 200.0, Instant.now()));

        // El tick del cruce y los 5 anteriores se persisten
        verify(tickRepository, times(6)).guardar(any(TickRecord.class));
        verify(signalRepository).guardar(any(SignalPayload.class));
    }

    @Test
    void falloPersistenciaTickNoDetieneProcesamiento() {
        doThrow(new RuntimeException("BD caída"))
            .when(tickRepository).guardar(any(TickRecord.class));

        service.procesarTick(new TickRecord("BTC", 60_000.0, Instant.now()));

        // El tick se procesó aunque la persistencia falló
        assertThat(service.getEstado("BTC")).isEqualTo("NORMAL");
    }

    @Test
    void falloPersistenciaSenalNoDetienePublicacion() {
        for (int i = 0; i < 5; i++) {
            service.procesarTick(new TickRecord("BTC", 100.0, Instant.now()));
        }

        doThrow(new RuntimeException("BD caída"))
            .when(signalRepository).guardar(any(SignalPayload.class));

        service.procesarTick(new TickRecord("BTC", 200.0, Instant.now()));

        // La señal se publicó a Kafka aunque la persistencia falló
        verify(signalPublisher).publicar(any(SignalPayload.class));
    }
}
