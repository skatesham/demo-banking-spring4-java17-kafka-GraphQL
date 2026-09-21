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

@Table("banking_transaction")
@Getter
public class BankingTransaction {

    @Id
    private final UUID id;

    @Column("request_id")
    private final UUID requestId;

    @Column("account_id")
    private final UUID accountId;

    @Column("operation_type")
    private final BankingOperationType operationType;

    private final BigDecimal amount;

    @Column("balance_after")
    private final BigDecimal balanceAfter;

    @Column("created_at")
    private final Instant createdAt;

    @Version
    private Long version;

    @PersistenceCreator
    public BankingTransaction(UUID id, UUID requestId, UUID accountId, BankingOperationType operationType,
            BigDecimal amount, BigDecimal balanceAfter, Instant createdAt, Long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.requestId = Objects.requireNonNull(requestId, "requestId must not be null");
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.operationType = Objects.requireNonNull(operationType, "operationType must not be null");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("amount must be positive");
        if (balanceAfter == null || balanceAfter.signum() < 0) throw new IllegalArgumentException("balanceAfter must not be negative");
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.version = version;
    }

    public static BankingTransaction from(BankingRequest request, BigDecimal balanceAfter) {
        return new BankingTransaction(UUID.randomUUID(), request.getId(), request.getAccountId(),
                request.getOperationType(), request.getAmount(), balanceAfter, Instant.now(), null);
    }

}
