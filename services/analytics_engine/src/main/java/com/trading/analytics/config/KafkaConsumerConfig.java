package com.trading.analytics.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.analytics.domain.model.TickRecord;
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
 * Configura el consumidor Kafka para el topic 'historical_ticks'.
 *
 * Como el productor (ingestion_bridge) envía con ADD_TYPE_INFO_HEADERS=false,
 * el JsonDeserializer se configura con el tipo target explícito por constructor.
 * Se usa ErrorHandlingDeserializer como wrapper para tolerar mensajes malformados.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9094}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:analytics-engine-group}")
    private String groupId;

    @Bean
    public ConsumerFactory<String, TickRecord> consumerFactory(ObjectMapper objectMapper) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        // ErrorHandlingDeserializer delega al JsonDeserializer interno
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);

        // JsonDeserializer configurado por constructor (no por properties para evitar conflicto)
        JsonDeserializer<TickRecord> jsonDeserializer = new JsonDeserializer<>(TickRecord.class, objectMapper);
        jsonDeserializer.addTrustedPackages("com.trading.analytics.domain.model");

        return new DefaultKafkaConsumerFactory<>(
            props,
            new StringDeserializer(),
            new ErrorHandlingDeserializer<>(jsonDeserializer)
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TickRecord> kafkaListenerContainerFactory(
            ConsumerFactory<String, TickRecord> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, TickRecord> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        return factory;
    }
}
