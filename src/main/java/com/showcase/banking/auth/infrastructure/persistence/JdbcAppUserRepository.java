package com.showcase.banking.auth.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import com.showcase.banking.auth.domain.AppUser;
import org.springframework.data.repository.ListCrudRepository;

public interface JdbcAppUserRepository extends ListCrudRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
}
