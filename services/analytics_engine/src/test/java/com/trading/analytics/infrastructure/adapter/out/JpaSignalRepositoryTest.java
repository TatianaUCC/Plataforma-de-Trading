package com.trading.analytics.infrastructure.adapter.out;

import com.trading.analytics.domain.model.SignalPayload;
import com.trading.analytics.infrastructure.jpa.TradingSignalEntity;
import com.trading.analytics.infrastructure.jpa.TradingSignalJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JpaSignalRepositoryTest {

    @Mock
    private TradingSignalJpaRepository jpaRepository;

    private JpaSignalRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaSignalRepository(jpaRepository);
    }

    @Test
    void guardarDelegaAlJpaRepository() {
        SignalPayload signal = new SignalPayload("BTC", "BUY", "Momentum Crossover", 200.0, 120.0);

        repository.guardar(signal);

        verify(jpaRepository).save(any(TradingSignalEntity.class));
    }

    @Test
    void guardarMapeaTodosLosCamposCorrectamente() {
        SignalPayload signal = new SignalPayload("ETH", "BUY", "Momentum Crossover", 3000.0, 2800.0);
        ArgumentCaptor<TradingSignalEntity> captor = ArgumentCaptor.forClass(TradingSignalEntity.class);

        repository.guardar(signal);

        verify(jpaRepository).save(captor.capture());
        TradingSignalEntity entity = captor.getValue();
        assertThat(entity.getAssetCode()).isEqualTo("ETH");
        assertThat(entity.getSignalType()).isEqualTo("BUY");
        assertThat(entity.getReason()).isEqualTo("Momentum Crossover");
        assertThat(entity.getCurrentPrice().doubleValue()).isEqualTo(3000.0);
        assertThat(entity.getSmaValue().doubleValue()).isEqualTo(2800.0);
        assertThat(entity.isNotified()).isFalse();
    }
}
