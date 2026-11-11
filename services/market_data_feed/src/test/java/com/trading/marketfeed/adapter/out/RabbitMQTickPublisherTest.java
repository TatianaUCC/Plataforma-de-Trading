package com.trading.marketfeed.adapter.out;

import com.trading.marketfeed.config.RabbitMQConfig;
import com.trading.marketfeed.domain.model.AssetCode;
import com.trading.marketfeed.domain.model.TickPriceRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitMQTickPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitMQTickPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new RabbitMQTickPublisher(rabbitTemplate);
    }

    @Test
    void publicarBtcDebeUsarRoutingKeyCrypto() {
        TickPriceRecord tick = new TickPriceRecord(new AssetCode("BTC"), 60_000.0, Instant.now());

        publisher.publicar(tick);

        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            org.mockito.ArgumentMatchers.eq(RabbitMQConfig.EXCHANGE_MARKET_TICKS),
            routingKeyCaptor.capture(),
            org.mockito.ArgumentMatchers.eq(tick)
        );
        assertThat(routingKeyCaptor.getValue()).isEqualTo("ticks.crypto.btc");
    }

    @Test
    void publicarEthDebeUsarRoutingKeyCrypto() {
        TickPriceRecord tick = new TickPriceRecord(new AssetCode("ETH"), 3_000.0, Instant.now());

        publisher.publicar(tick);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            org.mockito.ArgumentMatchers.eq(RabbitMQConfig.EXCHANGE_MARKET_TICKS),
            captor.capture(),
            org.mockito.ArgumentMatchers.eq(tick)
        );
        assertThat(captor.getValue()).isEqualTo("ticks.crypto.eth");
    }

    @Test
    void publicarEurusdDebeUsarRoutingKeyForex() {
        TickPriceRecord tick = new TickPriceRecord(new AssetCode("EURUSD"), 1.08, Instant.now());

        publisher.publicar(tick);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            org.mockito.ArgumentMatchers.eq(RabbitMQConfig.EXCHANGE_MARKET_TICKS),
            captor.capture(),
            org.mockito.ArgumentMatchers.eq(tick)
        );
        assertThat(captor.getValue()).isEqualTo("ticks.forex.eurusd");
    }

    @Test
    void publicarGooglDebeUsarRoutingKeyStock() {
        TickPriceRecord tick = new TickPriceRecord(new AssetCode("GOOGL"), 170.0, Instant.now());

        publisher.publicar(tick);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            org.mockito.ArgumentMatchers.eq(RabbitMQConfig.EXCHANGE_MARKET_TICKS),
            captor.capture(),
            org.mockito.ArgumentMatchers.eq(tick)
        );
        assertThat(captor.getValue()).isEqualTo("ticks.stock.googl");
    }

    @Test
    void publicarDebeEnviarAlExchangeCorrecto() {
        TickPriceRecord tick = new TickPriceRecord(new AssetCode("AMZN"), 185.0, Instant.now());

        publisher.publicar(tick);

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(
            exchangeCaptor.capture(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.eq(tick)
        );
        assertThat(exchangeCaptor.getValue()).isEqualTo("market_ticks");
    }
}
