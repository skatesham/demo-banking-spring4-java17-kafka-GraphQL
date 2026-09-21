package com.showcase.banking.operation.application;

import com.showcase.banking.account.application.GetAccount;
import com.showcase.banking.account.domain.Account;
import com.showcase.banking.operation.domain.BankingOperationType;
import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.domain.BankingTransaction;
import com.showcase.banking.operation.infrastructure.messaging.BankingOperationEvent;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingRequestRepository;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessBankingOperation {

    private static final Logger log = LoggerFactory.getLogger(ProcessBankingOperation.class);

    private final GetAccount getAccount;
    private final JdbcBankingRequestRepository requestRepository;
    private final JdbcBankingTransactionRepository transactionRepository;
    private final com.showcase.banking.account.infrastructure.persistence.JdbcAccountRepository accountRepository;

    public ProcessBankingOperation(GetAccount getAccount, JdbcBankingRequestRepository requestRepository,
            JdbcBankingTransactionRepository transactionRepository,
            com.showcase.banking.account.infrastructure.persistence.JdbcAccountRepository accountRepository) {
        this.getAccount = getAccount;
        this.requestRepository = requestRepository;
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void execute(BankingOperationEvent event) {
        BankingRequest request = requestRepository.findById(event.requestId())
                .orElseThrow(() -> new GetBankingRequest.BankingRequestNotFoundException(event.requestId()));
        if (request.isFinalized()) {
            log.info("Ignoring finalized banking operation: requestId={}, status={}", request.getId(), request.getStatus());
            return;
        }

        try {
            log.info("Processing banking operation: requestId={}, accountId={}, type={}, amount={}",
                    request.getId(), request.getAccountId(), request.getOperationType(), request.getAmount());
            Account account = getAccount.execute(request.getAccountId());
            if (request.getOperationType() == BankingOperationType.DEPOSIT) account.credit(request.getAmount());
            else account.debit(request.getAmount());

            accountRepository.save(account);
            transactionRepository.save(BankingTransaction.from(request, account.getBalance()));
            request.complete();
            log.info("Banking operation completed: requestId={}, accountId={}, balanceAfter={}",
                    request.getId(), account.getId(), account.getBalance());
        } catch (RuntimeException exception) {
            request.reject(exception.getMessage());
            log.warn("Banking operation rejected: requestId={}, accountId={}, reason={}",
                    request.getId(), request.getAccountId(), exception.getMessage());
        }
        requestRepository.save(request);
    }
}
