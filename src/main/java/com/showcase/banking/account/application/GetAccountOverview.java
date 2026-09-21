package com.showcase.banking.account.application;

import java.util.List;
import java.util.UUID;

import com.showcase.banking.account.domain.Account;
import com.showcase.banking.account.infrastructure.persistence.JdbcAccountRepository;
import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.domain.BankingTransaction;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingRequestRepository;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingTransactionRepository;
import org.springframework.stereotype.Service;

/**
 * Read model used by clients that need a compact account dashboard.
 */
@Service
public class GetAccountOverview {

    private final JdbcAccountRepository accountRepository;
    private final JdbcBankingTransactionRepository transactionRepository;
    private final JdbcBankingRequestRepository requestRepository;

    public GetAccountOverview(JdbcAccountRepository accountRepository,
            JdbcBankingTransactionRepository transactionRepository,
            JdbcBankingRequestRepository requestRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.requestRepository = requestRepository;
    }

    public AccountOverview execute(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new GetAccount.AccountNotFoundException(accountId));

        return new AccountOverview(
                account,
                transactionRepository.findTop10ByAccountIdOrderByCreatedAtDesc(accountId),
                requestRepository.findTop10ByAccountIdOrderByCreatedAtDesc(accountId));
    }

    public record AccountOverview(Account account, List<BankingTransaction> recentTransactions,
            List<BankingRequest> recentRequests) {
    }
}
