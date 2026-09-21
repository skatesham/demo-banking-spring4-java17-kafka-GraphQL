package com.showcase.banking.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados aceitos pelo endpoint de login no Swagger.")
public record LoginRequest(
        @Email @NotBlank @Schema(example = "ana@example.com") String email,
        @NotBlank @Schema(example = "senha-segura-123") String password) { }
