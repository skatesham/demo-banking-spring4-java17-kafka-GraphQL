package com.showcase.banking.operation.api;

import java.time.Instant;
import java.util.UUID;

import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.domain.BankingRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estado da solicitação assíncrona publicada no Kafka.")
public record BankingRequestResponse(
        @Schema(description = "UUIDv7 da solicitação.") UUID requestId,
        @Schema(description = "PENDING, COMPLETED ou REJECTED.") BankingRequestStatus status,
        @Schema(description = "Data de recebimento da intenção.") Instant createdAt,
        @Schema(description = "Data de processamento; nula enquanto PENDING.", nullable = true) Instant processedAt,
        @Schema(description = "Motivo da rejeição; preenchido apenas em REJECTED.", nullable = true) String rejectionReason) {
    public static BankingRequestResponse from(BankingRequest request) {
        return new BankingRequestResponse(request.getId(), request.getStatus(), request.getCreatedAt(),
                request.getProcessedAt(), request.getRejectionReason());
    }
}
