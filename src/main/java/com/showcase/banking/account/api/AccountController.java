package com.showcase.banking.account.api;

import java.util.UUID;

import com.showcase.banking.account.application.CreateAccount;
import com.showcase.banking.operation.api.BankingOperationRequest;
import com.showcase.banking.operation.api.BankingRequestResponse;
import com.showcase.banking.operation.application.RequestBankingOperation;
import com.showcase.banking.operation.domain.BankingOperationType;
import com.showcase.banking.security.AccountAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Accounts", description = "Todas as operações usam e validam a conta vinculada ao token Bearer.")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final CreateAccount createAccount;
    private final RequestBankingOperation requestBankingOperation;
    private final AccountAccess accountAccess;

    public AccountController(CreateAccount createAccount,
            RequestBankingOperation requestBankingOperation, AccountAccess accountAccess) {
        this.createAccount = createAccount;
        this.requestBankingOperation = requestBankingOperation;
        this.accountAccess = accountAccess;
    }

    @PostMapping
    @Operation(summary = "Cria a única conta do usuário autenticado", description = "Não recebe holderId: o dono é extraído do token.")
    public ResponseEntity<AccountResponse> create() {
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(createAccount.execute(accountAccess.currentUserId())));
    }

    @GetMapping("/{accountId}")
    @Operation(summary = "Consulta a própria conta")
    public AccountResponse get(@PathVariable UUID accountId) {
        return AccountResponse.from(accountAccess.ownedAccount(accountId));
    }

    @PostMapping("/{accountId}/deposits")
    @Operation(summary = "Solicita depósito na própria conta")
    public ResponseEntity<BankingRequestResponse> requestDeposit(@PathVariable UUID accountId,
            @Valid @RequestBody BankingOperationRequest request) {
        return accepted(requestBankingOperation.execute(accountAccess.ownedAccount(accountId).getId(), BankingOperationType.DEPOSIT, request.amount()));
    }

    @PostMapping("/{accountId}/withdrawals")
    @Operation(summary = "Solicita saque da própria conta")
    public ResponseEntity<BankingRequestResponse> requestWithdrawal(@PathVariable UUID accountId,
            @Valid @RequestBody BankingOperationRequest request) {
        return accepted(requestBankingOperation.execute(accountAccess.ownedAccount(accountId).getId(), BankingOperationType.WITHDRAWAL, request.amount()));
    }

    private static ResponseEntity<BankingRequestResponse> accepted(BankingRequestResponse response) {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
