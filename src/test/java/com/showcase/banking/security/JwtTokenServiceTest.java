package com.showcase.banking.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.showcase.banking.config.AppProperties;
import com.showcase.banking.shared.UuidV7;
import org.junit.jupiter.api.Test;

class JwtTokenServiceTest {
    private final JwtTokenService tokens = new JwtTokenService(properties());

    @Test
    void returnsTheAuthenticatedUserFromAValidToken() {
        var userId = UuidV7.next();
        assertThat(tokens.validate(tokens.create(userId))).isEqualTo(userId);
    }

    @Test
    void rejectsAChangedToken() {
        String token = tokens.create(UuidV7.next());
        assertThatThrownBy(() -> tokens.validate(token.substring(0, token.length() - 1) + "x"))
                .isInstanceOf(JwtTokenService.InvalidTokenException.class);
    }

    private static AppProperties properties() {
        AppProperties properties = new AppProperties();
        AppProperties.Security security = new AppProperties.Security();
        security.setJwtSecret("a-test-secret-that-is-long-enough-for-hs256");
        security.setJwtExpirationSeconds(60);
        properties.setSecurity(security);
        return properties;
    }
}
