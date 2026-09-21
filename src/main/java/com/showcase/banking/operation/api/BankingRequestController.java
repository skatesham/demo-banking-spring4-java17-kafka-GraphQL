package com.showcase.banking.operation.api;

import java.util.UUID;

import com.showcase.banking.operation.application.GetBankingRequest;
import com.showcase.banking.security.AccountAccess;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
@SecurityRequirement(name = "bearerAuth")
public class BankingRequestController {

    private final GetBankingRequest getBankingRequest;
    private final AccountAccess accountAccess;

    public BankingRequestController(GetBankingRequest getBankingRequest, AccountAccess accountAccess) {
        this.getBankingRequest = getBankingRequest;
        this.accountAccess = accountAccess;
    }

    @GetMapping("/{requestId}")
    @Operation(summary = "Consulta uma solicitação da própria conta")
    public BankingRequestResponse get(@PathVariable UUID requestId) {
        var request = getBankingRequest.execute(requestId);
        accountAccess.ownedAccount(request.getAccountId());
        return BankingRequestResponse.from(request);
    }
}
