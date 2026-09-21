package com.showcase.banking.operation.application;

import java.util.UUID;

import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.infrastructure.persistence.JdbcBankingRequestRepository;
import org.springframework.stereotype.Service;

@Service
public class GetBankingRequest {

    private final JdbcBankingRequestRepository requestRepository;

    public GetBankingRequest(JdbcBankingRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    public BankingRequest execute(UUID requestId) {
        return requestRepository.findById(requestId)
                .orElseThrow(() -> new BankingRequestNotFoundException(requestId));
    }

    public static class BankingRequestNotFoundException extends RuntimeException {
        public BankingRequestNotFoundException(UUID requestId) {
            super("Banking request %s was not found".formatted(requestId));
        }
    }
}
