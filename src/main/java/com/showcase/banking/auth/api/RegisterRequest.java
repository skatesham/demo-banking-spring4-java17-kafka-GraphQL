package com.showcase.banking.auth.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Credenciais usadas para criar o usuário. A senha nunca é retornada nem armazenada em texto puro.")
public record RegisterRequest(
        @Email @NotBlank @Schema(description = "E-mail único do usuário.", example = "ana@example.com") String email,
        @NotBlank @Size(min = 8, max = 72) @Schema(description = "Senha com pelo menos 8 caracteres.", example = "senha-segura-123") String password) { }
