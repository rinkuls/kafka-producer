package com.kafka.producer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.UUID;

@Service
public class KafkaSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSender.class);
    @Value("${avro.topic.name}")
    String topicName;

    @Value("${dlt.topic.name}")
    String dltTopicName;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(Object producerRecord) {
        String uniqueKey = producerRecord.getClass().getSimpleName() + "-" + UUID.randomUUID();

        try {
            ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, uniqueKey, producerRecord);
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onFailure(Throwable ex) {
                    LOGGER.error("Message failed to produce to Kafka topic {}", topicName);
                    sendToDLT(uniqueKey, producerRecord);
                }

                @Override
                public void onSuccess(SendResult<String, Object> result) {
                    LOGGER.info("Data - {} successfully produced to Kafka topic - {}", producerRecord, topicName);
                }
            });
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Unsupported message type for Avro serialization. Sending to DLT.", ex);
            sendToDLT(uniqueKey, producerRecord);
        }
    }

    private void sendToDLT(String key, Object producerRecord) {
        try {
            kafkaTemplate.send(dltTopicName, key, producerRecord).get();
            LOGGER.info("Message sent to DLT topic: {}", dltTopicName);
        } catch (Exception e) {
            LOGGER.error("Failed to send message to DLT topic: {}", dltTopicName, e);
        }
    }
}
