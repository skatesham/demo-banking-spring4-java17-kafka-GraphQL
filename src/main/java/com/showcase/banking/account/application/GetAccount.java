package com.showcase.banking.account.application;

import java.util.UUID;

import com.showcase.banking.account.domain.Account;
import com.showcase.banking.account.infrastructure.persistence.JdbcAccountRepository;
import org.springframework.stereotype.Service;

@Service
public class GetAccount {

    private final JdbcAccountRepository accountRepository;

    public GetAccount(JdbcAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account execute(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    public static class AccountNotFoundException extends RuntimeException {
        public AccountNotFoundException(UUID accountId) {
            super("Account %s was not found".formatted(accountId));
        }
    }
}
