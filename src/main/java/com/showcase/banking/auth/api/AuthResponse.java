package com.showcase.banking.auth.api;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sessão stateless. Use accessToken como Bearer token nas demais chamadas.")
public record AuthResponse(
        @Schema(description = "Token JWT assinado para a autenticação Bearer.") String accessToken,
        @Schema(description = "UUIDv7 da conta do usuário; é nulo até que a conta seja criada.", nullable = true) UUID accountId,
        @Schema(description = "Indica se POST /accounts pode ser chamado pelo usuário logado.") boolean canCreateAccount) { }
