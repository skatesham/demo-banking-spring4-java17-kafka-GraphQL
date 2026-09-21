package com.showcase.banking.auth.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.showcase.banking.shared.UuidV7;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

@Table("app_user")
@Getter
public class AppUser {
    @Id private final UUID id;
    private final String email;
    private final String passwordHash;
    private final Instant createdAt;
    @Version private Long version;

    @PersistenceCreator
    public AppUser(UUID id, String email, String passwordHash, Instant createdAt, Long version) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.version = version;
    }

    public static AppUser create(String email, String passwordHash) {
        return new AppUser(UuidV7.next(), email.trim().toLowerCase(), passwordHash, Instant.now(), null);
    }
}
