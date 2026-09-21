package com.showcase.banking.account.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.showcase.banking.account.domain.Account;
import com.showcase.banking.account.domain.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Representação da conta pertencente ao usuário autenticado.")
public record AccountResponse(
        @Schema(description = "UUIDv7 gerado pelo servidor.") UUID id,
        @Schema(description = "Estado atual da conta.") AccountStatus status,
        @Schema(description = "Saldo atual, com duas casas decimais.", example = "125.50") BigDecimal balance,
        @Schema(description = "Data e hora de criação em UTC.") Instant createdAt) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getStatus(),
                account.getBalance(), account.getCreatedAt());
    }
}
