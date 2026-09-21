package com.showcase.banking.account.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.showcase.banking.account.application.GetAccountOverview;
import com.showcase.banking.account.application.GetAccount;
import com.showcase.banking.account.domain.Account;
import com.showcase.banking.operation.domain.BankingRequest;
import com.showcase.banking.operation.domain.BankingTransaction;
import com.showcase.banking.security.AccountAccess;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

/**
 * GraphQL is intentionally a read API here. Commands remain REST endpoints so
 * their asynchronous 202 Accepted contract stays explicit.
 */
@Controller
public class AccountOverviewGraphQlController {

    private final GetAccountOverview getAccountOverview;
    private final AccountAccess accountAccess;

    public AccountOverviewGraphQlController(GetAccountOverview getAccountOverview, AccountAccess accountAccess) {
        this.getAccountOverview = getAccountOverview;
        this.accountAccess = accountAccess;
    }

    @QueryMapping
    public AccountOverviewView accountOverview(@Argument String accountId) {
        UUID parsedAccountId = parseAccountId(accountId);
        accountAccess.ownedAccount(parsedAccountId);
        GetAccountOverview.AccountOverview overview = getAccountOverview.execute(parsedAccountId);
        return AccountOverviewView.from(overview);
    }

    @GraphQlExceptionHandler
    GraphQLError handleInvalidAccountId(InvalidAccountIdException exception) {
        return GraphqlErrorBuilder.newError().message(exception.getMessage()).build();
    }

    @GraphQlExceptionHandler
    GraphQLError handleAccountNotFound(GetAccount.AccountNotFoundException exception) {
        return GraphqlErrorBuilder.newError().message(exception.getMessage()).build();
    }

    private static UUID parseAccountId(String accountId) {
        try {
            return UUID.fromString(accountId);
        } catch (IllegalArgumentException exception) {
            throw new InvalidAccountIdException(accountId);
        }
    }

    private static class InvalidAccountIdException extends RuntimeException {
        InvalidAccountIdException(String accountId) {
            super("accountId must be a valid UUID; received '%s'".formatted(accountId));
        }
    }

    public record AccountOverviewView(AccountView account, List<TransactionView> recentTransactions,
            List<RequestView> recentRequests) {
        static AccountOverviewView from(GetAccountOverview.AccountOverview overview) {
            return new AccountOverviewView(
                    AccountView.from(overview.account()),
                    overview.recentTransactions().stream().map(TransactionView::from).toList(),
                    overview.recentRequests().stream().map(RequestView::from).toList());
        }
    }

    public record AccountView(UUID id, String status, String balance) {
        static AccountView from(Account account) {
            return new AccountView(account.getId(), account.getStatus().name(),
                    account.getBalance().toPlainString());
        }
    }

    public record TransactionView(UUID id, UUID requestId, String operationType, String amount,
            String balanceAfter, String createdAt) {
        static TransactionView from(BankingTransaction transaction) {
            return new TransactionView(transaction.getId(), transaction.getRequestId(), transaction.getOperationType().name(),
                    transaction.getAmount().toPlainString(), transaction.getBalanceAfter().toPlainString(),
                    transaction.getCreatedAt().toString());
        }
    }

    public record RequestView(UUID id, String operationType, String amount, String status, String createdAt,
            String processedAt, String rejectionReason) {
        static RequestView from(BankingRequest request) {
            return new RequestView(request.getId(), request.getOperationType().name(), request.getAmount().toPlainString(),
                    request.getStatus().name(), request.getCreatedAt().toString(),
                    format(request.getProcessedAt()), request.getRejectionReason());
        }

        private static String format(Instant instant) {
            return instant == null ? null : instant.toString();
        }
    }
}
