package com.trading.notificationrouter.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.notificationrouter.domain.model.SignalEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Consumer Kafka para trading_signals.
 * Mismo patrón que analytics_engine: JsonDeserializer configurado por constructor,
 * ErrorHandlingDeserializer como wrapper, sin __TypeId__ header.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9094}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:notification-router-group}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, SignalEvent> consumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        JsonDeserializer<SignalEvent> jsonDeserializer =
            new JsonDeserializer<>(SignalEvent.class, objectMapper);
        jsonDeserializer.addTrustedPackages("com.trading.notificationrouter.domain.model");

        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, SignalEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, SignalEvent> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, SignalEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        return factory;
    }
}
