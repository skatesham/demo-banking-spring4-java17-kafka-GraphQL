package com.showcase.banking.operation.api;

import java.time.Instant;
import java.util.UUID;

import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.domain.BankingRequestStatus;

public record BankingRequestResponse(UUID requestId, BankingRequestStatus status, Instant createdAt,
        Instant processedAt, String rejectionReason) {
    public static BankingRequestResponse from(BankingRequest request) {
        return new BankingRequestResponse(request.getId(), request.getStatus(), request.getCreatedAt(),
                request.getProcessedAt(), request.getRejectionReason());
    }
}
