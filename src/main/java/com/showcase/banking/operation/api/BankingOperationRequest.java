package com.showcase.banking.operation.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record BankingOperationRequest(@NotNull @DecimalMin(value = "0.01") BigDecimal amount) {
}
