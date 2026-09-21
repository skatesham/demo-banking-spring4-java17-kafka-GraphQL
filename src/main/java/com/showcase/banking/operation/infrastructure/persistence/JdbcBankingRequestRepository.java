package com.showcase.banking.operation.infrastructure.persistence;

import java.util.UUID;
import java.util.List;

import com.showcase.banking.operation.domain.BankingRequest;
import org.springframework.data.repository.ListCrudRepository;

public interface JdbcBankingRequestRepository extends ListCrudRepository<BankingRequest, UUID> {
    List<BankingRequest> findTop10ByAccountIdOrderByCreatedAtDesc(UUID accountId);
}
