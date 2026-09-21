package com.showcase.banking.auth.api;

import com.showcase.banking.auth.application.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Cadastro e login. Login retorna o token Bearer e a conta vinculada, se existir.")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }
    @PostMapping("/register") @Operation(summary = "Cria um usuário sem conta")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request.email(), request.password()));
    }
    @PostMapping("/login") @Operation(summary = "Faz login e retorna um JWT Bearer e o accountId")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) { return authService.login(request.email(), request.password()); }
}
