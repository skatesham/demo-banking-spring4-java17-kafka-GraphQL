package com.showcase.banking.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

    @Bean
    NewTopic bankingOperationsTopic(AppProperties properties) {
        AppProperties.Kafka kafka = properties.getKafka();
        return TopicBuilder.name(kafka.getOperationsTopic())
                .partitions(kafka.getOperationsTopicPartitions())
                .replicas(kafka.getOperationsTopicReplicas()).build();
    }
}
