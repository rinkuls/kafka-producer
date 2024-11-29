package com.kafka.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerConfig.class);

    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    @Value("${avro.topic.name}")
    private String topicName;

    @Value("${dlt.topic.name}")
    private String dltTopicName;

    @Value("${spring.kafka.producer.properties.schema.registry.DockerComposeUrl}")
    private String dockerComposeSchemaRegistryUrl;

    @Value("${spring.kafka.producer.properties.schema.registry.url}")
    private String clusterSchemaRegistryUrl;

    @Value("${spring.kafka.producer.keySerializer}")
    private String keySerializer;

    @Value("${spring.kafka.producer.valueSerializer}")
    private String valueSerializer;

    @Value("${USE_DOCKER_COMPOSE:false}") // Environment variable to control schema registry URL
    private boolean useDockerCompose;

    /**
     * Primary KafkaTemplate for main topic (using Avro serialization)
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        LOGGER.info("Creating KafkaTemplate for main topic: {}", topicName);
        return new KafkaTemplate<>(producerFactory());
    }

    /**
     * KafkaTemplate specifically for DLT topic (using String serialization)
     */
    @Bean
    public KafkaTemplate<String, Object> dltKafkaTemplate() {
        LOGGER.info("Creating KafkaTemplate for DLT topic: {}", dltTopicName);
        return new KafkaTemplate<>(dltProducerFactory());
    }

    /**
     * NewTopic for main topic with retention policy
     */
    @Bean
    public NewTopic createTopic() {
        LOGGER.info("Creating main topic: {}", topicName);
        return new NewTopic(topicName, 3, (short) 1)
                .configs(Map.of(
                        "retention.ms", "604800000", // 7 days
                        "cleanup.policy", "delete", // Default: delete old messages
                        "retention.bytes", "-1"     // No size limit for retention
                ));
    }

    /**
     * NewTopic for DLT topic with retention policy
     */
    @Bean
    public NewTopic createDLTTopic() {
        LOGGER.info("Creating DLT topic: {}", dltTopicName);
        return new NewTopic(dltTopicName, 3, (short) 1)
                .configs(Map.of(
                        "retention.ms", "604800000", // 14 days
                        "cleanup.policy", "delete"
                ));
    }

    /**
     * Producer configs for main topic (Avro serialization)
     */
    @Bean
    public Map<String, Object> producerConfigs() {
        LOGGER.info("Loading producer configurations...");
        LOGGER.info("****************************************************************************************" +
                "*******************************************************************" +
                "***********************************************************" +
                "*********************below " +
                "is the value of useDockerCompose" + "--------------------" + useDockerCompose + "--------------------------------");
        String schemaRegistryUrl = useDockerCompose ? dockerComposeSchemaRegistryUrl : clusterSchemaRegistryUrl;
        LOGGER.info("Using schema registry URL: {}", schemaRegistryUrl);

        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        props.put("schema.registry.url", schemaRegistryUrl);
        props.put("auto.register.schemas", "false");
        return props;
    }

    /**
     * ProducerFactory for main topic
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        LOGGER.info("Creating ProducerFactory for main topic.");
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    /**
     * ProducerFactory specifically for DLT topic (String serialization)
     */
    @Bean
    public ProducerFactory<String, Object> dltProducerFactory() {
        LOGGER.info("Creating ProducerFactory for DLT topic.");
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
