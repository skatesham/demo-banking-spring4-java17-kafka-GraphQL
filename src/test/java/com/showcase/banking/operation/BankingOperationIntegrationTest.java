package com.showcase.banking.operation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import com.showcase.banking.auth.domain.AppUser;
import com.showcase.banking.auth.infrastructure.persistence.JdbcAppUserRepository;
import com.showcase.banking.shared.UuidV7;

import com.showcase.banking.TestcontainersConfiguration;
import com.showcase.banking.account.application.CreateAccount;
import com.showcase.banking.account.application.GetAccount;
import com.showcase.banking.account.domain.Account;
import com.showcase.banking.operation.api.BankingRequestResponse;
import com.showcase.banking.operation.application.ProcessBankingOperation;
import com.showcase.banking.operation.application.RequestBankingOperation;
import com.showcase.banking.operation.domain.BankingOperationType;
import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.domain.BankingRequestStatus;
import com.showcase.banking.operation.infrastructure.messaging.BankingOperationEvent;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingRequestRepository;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class BankingOperationIntegrationTest {

    @Autowired
    private CreateAccount createAccount;

    @Autowired
    private GetAccount getAccount;

    @Autowired
    private RequestBankingOperation requestBankingOperation;

    @Autowired
    private ProcessBankingOperation processBankingOperation;

    @Autowired
    private JdbcBankingRequestRepository requestRepository;

    @Autowired
    private JdbcBankingTransactionRepository transactionRepository;

    @Autowired
    private JdbcAppUserRepository userRepository;

    @Test
    void processesDepositPublishedToKafka() {
        Account account = createAccount.execute(createUser());

        BankingRequestResponse response = requestBankingOperation.execute(account.getId(), BankingOperationType.DEPOSIT,
                new BigDecimal("125.50"));
        BankingRequest request = awaitFinalized(response.requestId());

        assertThat(request.getStatus()).isEqualTo(BankingRequestStatus.COMPLETED);
        assertThat(getAccount.execute(account.getId()).getBalance()).isEqualByComparingTo("125.50");
        assertThat(transactionRepository.findByRequestId(request.getId())).isPresent();
    }

    @Test
    void rejectsWithdrawalWithInsufficientBalance() {
        Account account = createAccount.execute(createUser());

        BankingRequestResponse response = requestBankingOperation.execute(account.getId(), BankingOperationType.WITHDRAWAL,
                BigDecimal.ONE);
        BankingRequest request = awaitFinalized(response.requestId());

        assertThat(request.getStatus()).isEqualTo(BankingRequestStatus.REJECTED);
        assertThat(request.getRejectionReason()).contains("insufficient balance");
        assertThat(getAccount.execute(account.getId()).getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(transactionRepository.findByRequestId(request.getId())).isEmpty();
    }

    @Test
    void doesNotApplyTheSameEventTwice() {
        Account account = createAccount.execute(createUser());
        BankingRequest request = requestRepository.save(BankingRequest.pending(account.getId(), BankingOperationType.DEPOSIT,
                new BigDecimal("50.00")));
        BankingOperationEvent event = BankingOperationEvent.from(request);

        processBankingOperation.execute(event);
        processBankingOperation.execute(event);

        assertThat(getAccount.execute(account.getId()).getBalance()).isEqualByComparingTo("50.00");
        assertThat(transactionRepository.findByRequestId(request.getId())).isPresent();
        assertThat(requestRepository.findById(request.getId())).get()
                .extracting(BankingRequest::getStatus).isEqualTo(BankingRequestStatus.COMPLETED);
    }

    private BankingRequest awaitFinalized(UUID requestId) {
        Instant deadline = Instant.now().plus(Duration.ofSeconds(15));
        while (Instant.now().isBefore(deadline)) {
            BankingRequest request = requestRepository.findById(requestId).orElseThrow();
            if (request.isFinalized()) return request;
            try {
                Thread.sleep(100);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting for banking operation", exception);
            }
        }
        return fail("Banking request %s was not finalized within 15 seconds".formatted(requestId));
    }

    private UUID createUser() {
        return userRepository.save(AppUser.create("test-" + UuidV7.next() + "@example.com", "not-a-real-password-hash")).getId();
    }
}
