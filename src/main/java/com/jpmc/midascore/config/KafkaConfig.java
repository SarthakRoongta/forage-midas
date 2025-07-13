package com.jpmc.midascore.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.jpmc.midascore.foundation.Transaction;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, Transaction> producerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String brokers
    ) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Transaction> kafkaTemplate(
            ProducerFactory<String, Transaction> pf
    ) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConsumerFactory<String, Transaction> consumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String brokers
    ) {
        JsonDeserializer<Transaction> deser = new JsonDeserializer<>(Transaction.class);
        deser.addTrustedPackages("com.jpmc.midascore.foundation");
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deser
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Transaction>
            kafkaListenerContainerFactory(ConsumerFactory<String, Transaction> cf) {
        ConcurrentKafkaListenerContainerFactory<String, Transaction> f
                = new ConcurrentKafkaListenerContainerFactory<>();
        f.setConsumerFactory(cf);
        return f;
    }
}
