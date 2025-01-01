package com.kafka.producer.Kafka;

import com.kafka.producer.service.KafkaSender;
import com.rinkul.avro.schema.EmployeeRecord;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@EmbeddedKafka(
        partitions = 1,
        topics = {"${avro.topic.name}", "${dlt.topic.name}"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.producer.properties.schema.registry.url=mock://test"
})
@SpringBootTest
class KafkaProducerAvroIntegrationTest {

    @Value("${avro.topic.name}")
    private String topicName;
    @Value("${dlt.topic.name}")
    private String dltTopicName;

    @Autowired
    private KafkaSender kafkaSender;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, EmployeeRecord> consumer;
    private Consumer<String, String> dltConsumer;
    private MockSchemaRegistryClient mockSchemaRegistryClient;

    @BeforeEach
    void setup() {
        mockSchemaRegistryClient = new MockSchemaRegistryClient();

        // Register schema in mock registry
        try {
            mockSchemaRegistryClient.register(topicName + "-value", EmployeeRecord.getClassSchema());
        } catch (Exception e) {
            throw new RuntimeException("Failed to register schema in mock registry", e);
        }

        // Create topics explicitly

        var topics = embeddedKafkaBroker.getTopics();
        if (!topics.contains(dltTopicName)) {
            embeddedKafkaBroker.addTopics(dltTopicName);

        }
        if (!topics.contains(topicName)) {
            embeddedKafkaBroker.addTopics(topicName);

        }


        // Add delay to ensure topics are initialized
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted during Kafka initialization", e);
        }

        // Log topic readiness
        System.out.println("Topics available in embedded Kafka: " + embeddedKafkaBroker.getTopics());

        // Configure main topic consumer


    }

    @AfterEach
    void cleanup() {
        if (consumer != null) {
            consumer.close();
        }
        if (dltConsumer != null) {
            dltConsumer.close();
        }
        embeddedKafkaBroker.getKafkaServers().forEach(server -> server.shutdown());
        embeddedKafkaBroker.getKafkaServers().forEach(server -> server.awaitShutdown());
    }

    // @Test
    void testSendAvroMessageToKafka() {

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaAvroDeserializer.class);
        consumerProps.put("schema.registry.url", "mock://test");
        consumerProps.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, KafkaAvroDeserializer.class.getName());
        consumerProps.put("specific.avro.reader", "true");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Ensure consumer starts from the beginning

        DefaultKafkaConsumerFactory<String, EmployeeRecord> consumerFactory = new DefaultKafkaConsumerFactory<>(consumerProps);
        consumer = consumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, topicName);
        // Arrange
        EmployeeRecord employeeRecord = EmployeeRecord.newBuilder()
                .setEmpId(123L)
                .setFirstName("John")
                .setLastName("Doe")
                .setEmail("25test@gmail.com")
                .build();

        // Act
        kafkaSender.send(employeeRecord);

        // Assert
        ConsumerRecord<String, EmployeeRecord> singleRecord = KafkaTestUtils.getSingleRecord(consumer, topicName);
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.value()).isEqualTo(employeeRecord);
    }

    //@Test
    void testSendAvroMessageToKafkaDltTopic() {

        // Configure DLT consumer
        Map<String, Object> dltConsumerProps = KafkaTestUtils.consumerProps("dltTestGroup", "true", embeddedKafkaBroker);
        dltConsumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        dltConsumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        dltConsumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Ensure DLT consumer starts from beginning

        DefaultKafkaConsumerFactory<String, String> dltConsumerFactory = new DefaultKafkaConsumerFactory<>(dltConsumerProps);
        dltConsumer = dltConsumerFactory.createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(dltConsumer, true);

        // Arrange
        String testStudent = "testStudent , abc ,xyv";

        // Act
        kafkaSender.send(testStudent);

        // Assert
        ConsumerRecord<String, String> singleRecord = KafkaTestUtils.getSingleRecord(dltConsumer, dltTopicName);
        assertThat(singleRecord).isNotNull();
        assertThat(singleRecord.value()).isEqualTo(testStudent);
    }
}
