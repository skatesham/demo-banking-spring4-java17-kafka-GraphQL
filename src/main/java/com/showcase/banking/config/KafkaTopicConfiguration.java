package com.showcase.banking.config;

import com.showcase.banking.operation.infrastructure.messaging.BankingOperationPublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    NewTopic bankingOperationsTopic() {
        return TopicBuilder.name(BankingOperationPublisher.TOPIC).partitions(3).replicas(1).build();
    }
}
