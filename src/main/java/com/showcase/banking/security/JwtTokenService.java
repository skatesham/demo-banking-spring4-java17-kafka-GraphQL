package com.showcase.banking.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.showcase.banking.config.AppProperties;
import org.springframework.stereotype.Service;

/** Small HS256 JWT issuer/validator for the demo. Production secrets must come from a secret manager. */
@Service
public class JwtTokenService {
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtTokenService(AppProperties properties) {
        this.secret = properties.getSecurity().getJwtSecret().getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = properties.getSecurity().getJwtExpirationSeconds();
    }
    public String create(UUID userId) {
        long expiresAt = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + userId + "\",\"exp\":" + expiresAt + "}");
        String content = header + "." + payload;
        return content + "." + sign(content);
    }
    public UUID validate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3 || !MessageDigest.isEqual(sign(parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII),
                parts[2].getBytes(StandardCharsets.US_ASCII))) throw new InvalidTokenException();
        try {
            String payload = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
            String subject = payload.replaceAll(".*\\\"sub\\\":\\\"([^\\\"]+)\\\".*", "$1");
            String expires = payload.replaceAll(".*\\\"exp\\\":(\\d+).*", "$1");
            if (subject.equals(payload) || expires.equals(payload) || Instant.now().getEpochSecond() >= Long.parseLong(expires)) throw new InvalidTokenException();
            return UUID.fromString(subject);
        } catch (IllegalArgumentException exception) { throw new InvalidTokenException(); }
    }
    private String sign(String content) {
        try { Mac mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret, "HmacSHA256")); return URL_ENCODER.encodeToString(mac.doFinal(content.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception exception) { throw new IllegalStateException("Could not sign JWT", exception); }
    }
    private static String encode(String value) { return URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8)); }
    public static class InvalidTokenException extends RuntimeException { }
}
