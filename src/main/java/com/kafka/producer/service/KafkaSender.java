package com.kafka.producer.service;

import com.rinkul.avro.schema.StudentRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class KafkaSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSender.class);

    @Autowired
    private KafkaTemplate<String, StudentRecord> kafkaTemplate;

    @Value("${avro.topic.name}")
    String topicName;



    public void send(StudentRecord studentRecord) {
        ListenableFuture<SendResult<String, StudentRecord>> future = kafkaTemplate.send(topicName, String.valueOf(studentRecord.getEmpId()), studentRecord);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure(Throwable ex) {
                LOGGER.info("Message failed to produce" + topicName);

            }

            @Override
            public void onSuccess(SendResult<String, StudentRecord> result) {
                LOGGER.info("Data - " + studentRecord + " Avro message successfully produced and sent to Kafka Topic - " + topicName);

            }
        });


    }
}
