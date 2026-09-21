package com.showcase.banking.operation.application;

import java.math.BigDecimal;
import java.util.UUID;

import com.showcase.banking.account.application.GetAccount;
import com.showcase.banking.account.domain.Account;
import com.showcase.banking.operation.api.BankingRequestResponse;
import com.showcase.banking.operation.domain.BankingOperationType;
import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.infrastructure.messaging.BankingOperationPublisher;
import com.showcase.banking.operation.infrastructure.messaging.BankingOperationEvent;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RequestBankingOperation {

    private static final Logger log = LoggerFactory.getLogger(RequestBankingOperation.class);

    private final GetAccount getAccount;
    private final JdbcBankingRequestRepository requestRepository;
    private final BankingOperationPublisher publisher;

    public RequestBankingOperation(GetAccount getAccount, JdbcBankingRequestRepository requestRepository,
            BankingOperationPublisher publisher) {
        this.getAccount = getAccount;
        this.requestRepository = requestRepository;
        this.publisher = publisher;
    }

    public BankingRequestResponse execute(UUID accountId, BankingOperationType operationType, BigDecimal amount) {
        Account account = getAccount.execute(accountId);
        BankingRequest request = requestRepository.save(BankingRequest.pending(accountId, operationType, amount));
        log.info("Banking operation requested: requestId={}, accountId={}, type={}, amount={}",
                request.getId(), accountId, operationType, amount);
        publisher.publish(account.getHolderId().toString(), BankingOperationEvent.from(request));
        return BankingRequestResponse.from(request);
    }
}
