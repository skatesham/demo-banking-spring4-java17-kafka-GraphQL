package com.showcase.banking.account.api;

import java.util.UUID;

import com.showcase.banking.account.application.CreateAccount;
import com.showcase.banking.account.application.GetAccount;
import com.showcase.banking.operation.api.BankingOperationRequest;
import com.showcase.banking.operation.api.BankingRequestResponse;
import com.showcase.banking.operation.application.RequestBankingOperation;
import com.showcase.banking.operation.domain.BankingOperationType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final CreateAccount createAccount;
    private final GetAccount getAccount;
    private final RequestBankingOperation requestBankingOperation;

    public AccountController(CreateAccount createAccount, GetAccount getAccount,
            RequestBankingOperation requestBankingOperation) {
        this.createAccount = createAccount;
        this.getAccount = getAccount;
        this.requestBankingOperation = requestBankingOperation;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(createAccount.execute(request.holderId())));
    }

    @GetMapping("/{accountId}")
    public AccountResponse get(@PathVariable UUID accountId) {
        return AccountResponse.from(getAccount.execute(accountId));
    }

    @PostMapping("/{accountId}/deposits")
    public ResponseEntity<BankingRequestResponse> requestDeposit(@PathVariable UUID accountId,
            @Valid @RequestBody BankingOperationRequest request) {
        return accepted(requestBankingOperation.execute(accountId, BankingOperationType.DEPOSIT, request.amount()));
    }

    @PostMapping("/{accountId}/withdrawals")
    public ResponseEntity<BankingRequestResponse> requestWithdrawal(@PathVariable UUID accountId,
            @Valid @RequestBody BankingOperationRequest request) {
        return accepted(requestBankingOperation.execute(accountId, BankingOperationType.WITHDRAWAL, request.amount()));
    }

    private static ResponseEntity<BankingRequestResponse> accepted(BankingRequestResponse response) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
