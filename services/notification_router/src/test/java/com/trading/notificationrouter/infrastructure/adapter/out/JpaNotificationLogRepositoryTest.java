package com.trading.notificationrouter.infrastructure.adapter.out;

import com.trading.notificationrouter.domain.model.SignalEvent;
import com.trading.notificationrouter.infrastructure.jpa.NotificationLogEntity;
import com.trading.notificationrouter.infrastructure.jpa.NotificationLogJpaRepository;
import com.trading.notificationrouter.infrastructure.jpa.TradingSignalLookupEntity;
import com.trading.notificationrouter.infrastructure.jpa.TradingSignalLookupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaNotificationLogRepositoryTest {

    @Mock
    private NotificationLogJpaRepository logRepo;
    @Mock
    private TradingSignalLookupRepository signalRepo;

    private JpaNotificationLogRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaNotificationLogRepository(logRepo, signalRepo);
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private TradingSignalLookupEntity stubEntity(long id) {
        TradingSignalLookupEntity e = new TradingSignalLookupEntity() {};
        try {
            var f = TradingSignalLookupEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(e, id);
        } catch (Exception ex) { throw new RuntimeException(ex); }
        return e;
    }

    // ── Pruebas de búsqueda precisa con los 4 criterios ──────────────────────

    @Test
    void debeUsarLosCuatroCriteriosParaBuscarLaSenal() {
        // Arrange
        SignalEvent signal = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);
        when(signalRepo.findMostRecentMatch(
            eq("BTC"), eq("BUY"),
            eq(BigDecimal.valueOf(60000.0)),
            eq(BigDecimal.valueOf(43000.0))
        )).thenReturn(Optional.of(stubEntity(1L)));

        // Act
        repository.registrarDespacho(signal, "EMAIL");

        // Assert — se invocó con los 4 criterios exactos
        verify(signalRepo).findMostRecentMatch(
            "BTC", "BUY",
            BigDecimal.valueOf(60000.0),
            BigDecimal.valueOf(43000.0)
        );
        verify(logRepo).save(any(NotificationLogEntity.class));
    }

    @Test
    void noDebeAsociarConSenalDelMismoActivoConPrecioDiferente() {
        // Arrange — llega evento BTC precio=60000 pero en BD solo hay BTC precio=55000
        SignalEvent signalRecibida = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        // La query con 4 criterios no encuentra match para precio=60000
        when(signalRepo.findMostRecentMatch(
            eq("BTC"), eq("BUY"),
            eq(BigDecimal.valueOf(60000.0)),
            eq(BigDecimal.valueOf(43000.0))
        )).thenReturn(Optional.empty());

        // Act
        repository.registrarDespacho(signalRecibida, "EMAIL");

        // Assert — no persiste porque no hay señal con ese precio exacto
        verify(logRepo, never()).save(any());
    }

    @Test
    void noDebeAsociarConSenalDelMismoActivoConSmaDiferente() {
        // Arrange — misma señal BTC pero con SMA distinta
        SignalEvent signalRecibida = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 50000.0);

        when(signalRepo.findMostRecentMatch(
            eq("BTC"), eq("BUY"),
            eq(BigDecimal.valueOf(60000.0)),
            eq(BigDecimal.valueOf(50000.0))
        )).thenReturn(Optional.empty());

        // Act
        repository.registrarDespacho(signalRecibida, "CONSOLE");

        // Assert
        verify(logRepo, never()).save(any());
    }

    @Test
    void debeGuardarElSignalIdCorrecto() {
        // Arrange — dos señales BTC en BD: id=1 (precio=55000) e id=2 (precio=60000)
        // La señal recibida tiene precio=60000 → debe asociarse al id=2
        SignalEvent signal = new SignalEvent("BTC", "BUY", "Momentum Crossover", 60000.0, 43000.0);

        when(signalRepo.findMostRecentMatch(
            eq("BTC"), eq("BUY"),
            eq(BigDecimal.valueOf(60000.0)),
            eq(BigDecimal.valueOf(43000.0))
        )).thenReturn(Optional.of(stubEntity(2L)));

        ArgumentCaptor<NotificationLogEntity> captor =
            ArgumentCaptor.forClass(NotificationLogEntity.class);

        // Act
        repository.registrarDespacho(signal, "EMAIL");

        // Assert — el id grabado es 2, no 1
        verify(logRepo).save(captor.capture());
        assertThat(captor.getValue().getSignalId()).isEqualTo(2L);
    }

    @Test
    void debeOmitirElLogSinExcepcionCuandoNoEncuentraLaSenal() {
        // Arrange
        SignalEvent signal = new SignalEvent("ETH", "BUY", "Momentum Crossover", 3000.0, 2500.0);
        when(signalRepo.findMostRecentMatch(any(), any(), any(), any()))
            .thenReturn(Optional.empty());

        // Act — no debe lanzar excepción
        repository.registrarDespacho(signal, "EMAIL");

        // Assert
        verify(logRepo, never()).save(any());
    }
}
