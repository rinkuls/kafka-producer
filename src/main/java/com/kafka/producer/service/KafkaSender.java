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
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void send(Object producerRecord) {
        String uniqueKey = producerRecord.getClass().getSimpleName() + "-" + UUID.randomUUID();

        ListenableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topicName, uniqueKey, producerRecord);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                LOGGER.info("*********************************Message failed to produce to kafka for this topic*************************************** " + topicName);

            }

            @Override
            public void onSuccess(SendResult<String, Object> result) {
                LOGGER.info("Data - " + producerRecord + " Avro message successfully produced and sent to Kafka Topic - " + topicName);

            }
        });


    }
}
