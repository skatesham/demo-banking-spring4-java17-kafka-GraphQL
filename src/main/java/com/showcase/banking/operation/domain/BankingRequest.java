package com.showcase.banking.operation.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("banking_request")
@Getter
public class BankingRequest {

    @Id
    private final UUID id;

    @Column("account_id")
    private final UUID accountId;

    @Column("operation_type")
    private final BankingOperationType operationType;

    private final BigDecimal amount;
    private BankingRequestStatus status;

    @Column("created_at")
    private final Instant createdAt;

    @Column("processed_at")
    private Instant processedAt;

    @Column("rejection_reason")
    private String rejectionReason;

    @Version
    private Long version;

    @PersistenceCreator
    public BankingRequest(UUID id, UUID accountId, BankingOperationType operationType, BigDecimal amount,
            BankingRequestStatus status, Instant createdAt, Instant processedAt, String rejectionReason, Long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.operationType = Objects.requireNonNull(operationType, "operationType must not be null");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
        this.amount = amount;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.processedAt = processedAt;
        this.rejectionReason = rejectionReason;
        this.version = version;
    }

    public static BankingRequest pending(UUID accountId, BankingOperationType operationType, BigDecimal amount) {
        return new BankingRequest(UUID.randomUUID(), accountId, operationType, amount,
                BankingRequestStatus.PENDING, Instant.now(), null, null, null);
    }

    public boolean isFinalized() { return status != BankingRequestStatus.PENDING; }

    public void complete() {
        ensurePending();
        status = BankingRequestStatus.COMPLETED;
        processedAt = Instant.now();
    }

    public void reject(String reason) {
        ensurePending();
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason must not be blank");
        status = BankingRequestStatus.REJECTED;
        rejectionReason = reason;
        processedAt = Instant.now();
    }

    private void ensurePending() {
        if (isFinalized()) throw new IllegalStateException("request %s is already finalized".formatted(id));
    }
}
