package com.trading.analytics.infrastructure.adapter.out;

import com.trading.analytics.domain.model.TickRecord;
import com.trading.analytics.infrastructure.jpa.TickRecordEntity;
import com.trading.analytics.infrastructure.jpa.TickRecordJpaRepository;
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
class JpaTickRecordRepositoryTest {

    @Mock
    private TickRecordJpaRepository jpaRepository;

    private JpaTickRecordRepository repository;

    @BeforeEach
    void setUp() {
        repository = new JpaTickRecordRepository(jpaRepository);
    }

    @Test
    void guardarDelegaAlJpaRepository() {
        TickRecord tick = new TickRecord("BTC", 60_000.0, Instant.now());

        repository.guardar(tick);

        verify(jpaRepository).save(any(TickRecordEntity.class));
    }

    @Test
    void guardarMapeaTodosLosCamposCorrectamente() {
        Instant now = Instant.now();
        TickRecord tick = new TickRecord("ETH", 3000.0, now);
        ArgumentCaptor<TickRecordEntity> captor = ArgumentCaptor.forClass(TickRecordEntity.class);

        repository.guardar(tick);

        verify(jpaRepository).save(captor.capture());
        TickRecordEntity entity = captor.getValue();
        assertThat(entity.getAssetCode()).isEqualTo("ETH");
        assertThat(entity.getPrice().doubleValue()).isEqualTo(3000.0);
        assertThat(entity.getProcessedAt()).isEqualTo(now);
    }
}
