package com.showcase.banking.operation.api;

import java.util.UUID;

import com.showcase.banking.operation.application.GetBankingRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/requests")
public class BankingRequestController {

    private final GetBankingRequest getBankingRequest;

    public BankingRequestController(GetBankingRequest getBankingRequest) {
        this.getBankingRequest = getBankingRequest;
    }

    @GetMapping("/{requestId}")
    public BankingRequestResponse get(@PathVariable UUID requestId) {
        return BankingRequestResponse.from(getBankingRequest.execute(requestId));
    }
}
