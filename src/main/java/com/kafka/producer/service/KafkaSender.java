package com.kafka.producer.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class KafkaSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSender.class);

    @Value("${avro.topic.name}")
    String topicName;

    @Value("${dlt.topic.name}")
    String dltTopicName;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaTemplate<String, Object> dltKafkaTemplate;

    public void send(Object producerRecord) {
        String uniqueKey = producerRecord.getClass().getSimpleName() + "-" + UUID.randomUUID();

        try {
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, uniqueKey, producerRecord);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    LOGGER.error("Message failed to produce to Kafka topic {}", topicName, ex);
                    sendToDLT(uniqueKey, producerRecord.toString()); // Send as a String to the DLT
                } else {
                    LOGGER.info("Data - {} successfully produced to Kafka topic - {}", producerRecord, topicName);
                }
            });
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Unsupported message type for Avro serialization. Sending to DLT.", ex);
            sendToDLT(uniqueKey, producerRecord.toString());
        }
    }


    private void sendToDLT(String key, String producerRecord) {
        try {

            CompletableFuture<SendResult<String, Object>> future = dltKafkaTemplate.send(topicName, key, producerRecord);
            LOGGER.info("Message sent to DLT topic: {}", dltTopicName);


            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    LOGGER.error("Message failed to produce to Kafka topic {}", topicName, ex);
                    sendToDLT(key, producerRecord); // Send as a String to the DLT
                } else {
                    LOGGER.info("Data - {} successfully produced to Kafka topic - {}", producerRecord, topicName);
                }
            });

        } catch (Exception e) {
            LOGGER.error("Failed to send message to DLT topic: {}", dltTopicName, e);
        }
    }
}
