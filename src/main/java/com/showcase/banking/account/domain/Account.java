package com.showcase.banking.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.showcase.banking.shared.UuidV7;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("account")
@Getter
public class Account {

    @Id
    private final UUID id;

    @Column("user_id")
    private final UUID userId;

    private AccountStatus status;
    private BigDecimal balance;

    @Column("created_at")
    private final Instant createdAt;

    @Version
    private Long version;

    public Account(UUID id, UUID userId, AccountStatus status, BigDecimal balance, Instant createdAt) {
        this(id, userId, status, balance, createdAt, null);
    }

    @PersistenceCreator
    public Account(UUID id, UUID userId, AccountStatus status, BigDecimal balance, Instant createdAt, Long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.balance = requireNonNegative(balance);
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.version = version;
    }

    public static Account create(UUID userId) {
        return new Account(UuidV7.next(), userId, AccountStatus.ACTIVE, BigDecimal.ZERO, Instant.now());
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(requirePositive(amount));
    }

    public void debit(BigDecimal amount) {
        BigDecimal withdrawal = requirePositive(amount);
        ensureActive();
        if (balance.compareTo(withdrawal) < 0) {
            throw new InsufficientBalanceException(id, withdrawal);
        }
        balance = balance.subtract(withdrawal);
    }

    public void deactivate() {
        status = AccountStatus.INACTIVE;
    }

    private void ensureActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountInactiveException(id);
        }
    }

    private static BigDecimal requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        return amount;
    }

    private static BigDecimal requireNonNegative(BigDecimal amount) {
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("balance must not be negative");
        }
        return amount;
    }
}
