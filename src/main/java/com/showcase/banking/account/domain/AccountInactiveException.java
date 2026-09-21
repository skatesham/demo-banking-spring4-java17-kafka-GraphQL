package com.showcase.banking.account.domain;

import java.util.UUID;

public class AccountInactiveException extends RuntimeException {

    public AccountInactiveException(UUID accountId) {
        super("Account %s is inactive".formatted(accountId));
    }
}
