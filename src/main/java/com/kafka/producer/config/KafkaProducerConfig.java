package com.kafka.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${avro.topic.name}")
    private String topicName;

    @Value("${dlt.topic.name}")
    private String dltTopicName;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.producer.keySerializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.valueSerializer}")
    private String valueSerializer;

    // Primary KafkaTemplate for main topic (using Avro serialization)
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // KafkaTemplate specifically for DLT topic (using String serialization)
    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate() {
        return new KafkaTemplate<>(dltProducerFactory());
    }

    // NewTopic for main topic
    @Bean
    public NewTopic createTopic() {
        return new NewTopic(topicName, 3, (short) 1);
    }

    // NewTopic for DLT topic
    @Bean
    public NewTopic createDLTTopic() {
        return new NewTopic(dltTopicName, 3, (short) 1);
    }

    // Producer configs for main topic (Avro serialization)
    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("auto.register.schemas", "true");
        return props;
    }

    // ProducerFactory for main topic
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    // ProducerFactory specifically for DLT topic (String serialization)
    @Bean
    public ProducerFactory<String, Object> dltProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
