package com.showcase.banking.account.infrastructure.persistence;

import java.util.UUID;

import com.showcase.banking.account.domain.Account;
import org.springframework.data.repository.ListCrudRepository;

public interface JdbcAccountRepository extends ListCrudRepository<Account, UUID> {
}
