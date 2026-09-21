package com.showcase.banking.operation.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Valor positivo da movimentação; a conta vem da URL e é conferida contra o token.")
public record BankingOperationRequest(
        @NotNull @DecimalMin(value = "0.01") @Schema(description = "Valor maior ou igual a 0.01.", example = "125.50") BigDecimal amount) {
}
