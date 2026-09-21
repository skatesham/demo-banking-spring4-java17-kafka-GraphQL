package com.showcase.banking.account.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.showcase.banking.account.domain.Account;
import com.showcase.banking.account.domain.AccountStatus;

public record AccountResponse(UUID id, UUID holderId, AccountStatus status, BigDecimal balance, Instant createdAt) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getHolderId(), account.getStatus(),
                account.getBalance(), account.getCreatedAt());
    }
}
