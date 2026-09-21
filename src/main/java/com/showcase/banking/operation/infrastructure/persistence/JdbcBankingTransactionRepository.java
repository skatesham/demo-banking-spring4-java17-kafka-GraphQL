package com.showcase.banking.operation.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import com.showcase.banking.operation.domain.BankingTransaction;
import org.springframework.data.repository.ListCrudRepository;

public interface JdbcBankingTransactionRepository extends ListCrudRepository<BankingTransaction, UUID> {
    Optional<BankingTransaction> findByRequestId(UUID requestId);

    List<BankingTransaction> findTop10ByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
