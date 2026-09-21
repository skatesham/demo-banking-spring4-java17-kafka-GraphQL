package com.showcase.banking.security;

import java.util.UUID;

import com.showcase.banking.account.application.GetAccount;
import com.showcase.banking.account.domain.Account;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountAccess {
    private final GetAccount getAccount;
    public AccountAccess(GetAccount getAccount) { this.getAccount = getAccount; }
    public UUID currentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UUID userId) return userId;
        throw new AccessDeniedException("Authentication is required");
    }
    public Account ownedAccount(UUID accountId) {
        Account account = getAccount.execute(accountId);
        if (!account.getUserId().equals(currentUserId())) throw new AccessDeniedException("This account does not belong to the logged-in user");
        return account;
    }
}
