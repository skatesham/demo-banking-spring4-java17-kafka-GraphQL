package com.showcase.banking.operation.infrastructure.messaging;

import com.showcase.banking.config.AppProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BankingOperationPublisher {

    private static final Logger log = LoggerFactory.getLogger(BankingOperationPublisher.class);

    private final KafkaTemplate<String, BankingOperationEvent> kafkaTemplate;
    private final String operationsTopic;

    public BankingOperationPublisher(KafkaTemplate<String, BankingOperationEvent> kafkaTemplate, AppProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.operationsTopic = properties.getKafka().getOperationsTopic();
    }

    public void publish(String accountId, BankingOperationEvent event) {
        kafkaTemplate.send(operationsTopic, accountId, event).whenComplete((result, exception) -> {
            if (exception == null) {
                log.info("Banking operation event published: requestId={}, accountId={}, topic={}",
                        event.requestId(), event.accountId(), operationsTopic);
            } else {
                log.error("Failed to publish banking operation event: requestId={}, accountId={}, topic={}",
                        event.requestId(), event.accountId(), operationsTopic, exception);
            }
        });
    }
}
