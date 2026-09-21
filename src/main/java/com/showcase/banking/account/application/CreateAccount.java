package com.showcase.banking.account.application;

import java.util.UUID;

import com.showcase.banking.account.domain.Account;
import com.showcase.banking.account.infrastructure.persistence.JdbcAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateAccount {

    private static final Logger log = LoggerFactory.getLogger(CreateAccount.class);

    private final JdbcAccountRepository accountRepository;

    public CreateAccount(JdbcAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account execute(UUID holderId) {
        Account account = accountRepository.save(Account.create(holderId));
        log.info("Account created: accountId={}, holderId={}", account.getId(), account.getHolderId());
        return account;
    }
}
