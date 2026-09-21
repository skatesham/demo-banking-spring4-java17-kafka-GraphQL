package com.showcase.banking.account.domain;

import java.math.BigDecimal;
import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(UUID accountId, BigDecimal requestedAmount) {
        super("Account %s has insufficient balance for withdrawal of %s".formatted(accountId, requestedAmount));
    }
}
