package com.showcase.banking.auth.application;

import java.util.Locale;

import com.showcase.banking.account.infrastructure.persistence.JdbcAccountRepository;
import com.showcase.banking.auth.api.AuthResponse;
import com.showcase.banking.auth.domain.AppUser;
import com.showcase.banking.auth.infrastructure.persistence.JdbcAppUserRepository;
import com.showcase.banking.security.JwtTokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final JdbcAppUserRepository userRepository;
    private final JdbcAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokens;

    public AuthService(JdbcAppUserRepository userRepository, JdbcAccountRepository accountRepository,
            PasswordEncoder passwordEncoder, JwtTokenService tokens) {
        this.userRepository = userRepository; this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder; this.tokens = tokens;
    }

    public AuthResponse register(String email, String password) {
        String normalizedEmail = normalize(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) throw new EmailAlreadyRegisteredException();
        AppUser user = userRepository.save(AppUser.create(normalizedEmail, passwordEncoder.encode(password)));
        return response(user);
    }

    public AuthResponse login(String email, String password) {
        AppUser user = userRepository.findByEmail(normalize(email)).orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) throw new InvalidCredentialsException();
        return response(user);
    }

    private AuthResponse response(AppUser user) {
        var account = accountRepository.findByUserId(user.getId());
        return new AuthResponse(tokens.create(user.getId()), account.map(a -> a.getId()).orElse(null), account.isEmpty());
    }
    private static String normalize(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    public static class InvalidCredentialsException extends RuntimeException { public InvalidCredentialsException() { super("Invalid email or password"); } }
    public static class EmailAlreadyRegisteredException extends RuntimeException { public EmailAlreadyRegisteredException() { super("Email is already registered"); } }
}
