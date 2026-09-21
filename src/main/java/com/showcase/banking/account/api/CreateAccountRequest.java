package com.showcase.banking.account.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(@NotNull UUID holderId) {
}
