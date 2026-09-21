package com.showcase.banking.operation.infrastructure.messaging;

import com.showcase.banking.operation.application.ProcessBankingOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BankingOperationConsumer {

    private static final Logger log = LoggerFactory.getLogger(BankingOperationConsumer.class);

    private final ProcessBankingOperation processBankingOperation;

    public BankingOperationConsumer(ProcessBankingOperation processBankingOperation) {
        this.processBankingOperation = processBankingOperation;
    }

    @KafkaListener(topics = BankingOperationPublisher.TOPIC)
    public void consume(BankingOperationEvent event) {
        log.info("Banking operation event received: requestId={}, accountId={}, type={}",
                event.requestId(), event.accountId(), event.operationType());
        processBankingOperation.execute(event);
    }
}
