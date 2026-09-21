package com.showcase.banking.operation.infrastructure.messaging;

import java.math.BigDecimal;
import java.util.UUID;

import com.showcase.banking.operation.domain.BankingOperationType;
import com.showcase.banking.operation.domain.BankingRequest;

public record BankingOperationEvent(UUID requestId, UUID accountId, BankingOperationType operationType, BigDecimal amount) {
    public static BankingOperationEvent from(BankingRequest request) {
        return new BankingOperationEvent(request.getId(), request.getAccountId(), request.getOperationType(), request.getAmount());
    }
}
