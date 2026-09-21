package com.showcase.banking.operation.infrastructure.messaging;

import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BankingOperationPublisher {

    public static final String TOPIC = "banking.operations";

    private static final Logger log = LoggerFactory.getLogger(BankingOperationPublisher.class);

    private final KafkaTemplate<String, BankingOperationEvent> kafkaTemplate;

    public BankingOperationPublisher(KafkaTemplate<String, BankingOperationEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(String holderId, BankingOperationEvent event) {
        kafkaTemplate.send(TOPIC, holderId, event).whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Banking operation event published: requestId={}, accountId={}, topic={}",
                        event.requestId(), event.accountId(), TOPIC);
            } else {
                log.error("Failed to publish banking operation event: requestId={}, accountId={}, topic={}",
                        event.requestId(), event.accountId(), TOPIC, exception);
            }
        });
    }
}
