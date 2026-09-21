package com.showcase.banking.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/** Centralizes all application-owned settings loaded from application.yml or environment variables. */
@Component("appProperties")
@Validated
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    @NotBlank private String name;
    @Valid private Security security = new Security();
    @Valid private Kafka kafka = new Kafka();
    @Valid private OpenApi openapi = new OpenApi();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }
    public Kafka getKafka() { return kafka; }
    public void setKafka(Kafka kafka) { this.kafka = kafka; }
    public OpenApi getOpenapi() { return openapi; }
    public void setOpenapi(OpenApi openapi) { this.openapi = openapi; }

    public static class Security {
        @NotBlank private String jwtSecret;
        @Positive private long jwtExpirationSeconds;
        public String getJwtSecret() { return jwtSecret; }
        public void setJwtSecret(String jwtSecret) { this.jwtSecret = jwtSecret; }
        public long getJwtExpirationSeconds() { return jwtExpirationSeconds; }
        public void setJwtExpirationSeconds(long jwtExpirationSeconds) { this.jwtExpirationSeconds = jwtExpirationSeconds; }
    }
    public static class Kafka {
        @NotBlank private String operationsTopic;
        @Positive private int operationsTopicPartitions;
        @Positive private short operationsTopicReplicas;
        @NotBlank private String consumerGroupId;
        public String getOperationsTopic() { return operationsTopic; }
        public void setOperationsTopic(String operationsTopic) { this.operationsTopic = operationsTopic; }
        public int getOperationsTopicPartitions() { return operationsTopicPartitions; }
        public void setOperationsTopicPartitions(int operationsTopicPartitions) { this.operationsTopicPartitions = operationsTopicPartitions; }
        public short getOperationsTopicReplicas() { return operationsTopicReplicas; }
        public void setOperationsTopicReplicas(short operationsTopicReplicas) { this.operationsTopicReplicas = operationsTopicReplicas; }
        public String getConsumerGroupId() { return consumerGroupId; }
        public void setConsumerGroupId(String consumerGroupId) { this.consumerGroupId = consumerGroupId; }
    }
    public static class OpenApi {
        @NotBlank private String title;
        @NotBlank private String version;
        @NotBlank private String description;
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
