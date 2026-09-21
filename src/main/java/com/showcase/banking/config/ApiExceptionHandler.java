package com.showcase.banking.config;

import com.showcase.banking.account.application.CreateAccount;
import com.showcase.banking.account.application.GetAccount;
import com.showcase.banking.auth.application.AuthService;
import com.showcase.banking.operation.application.GetBankingRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({GetAccount.AccountNotFoundException.class, GetBankingRequest.BankingRequestNotFoundException.class})
    ProblemDetail notFound(RuntimeException exception) { return problem(HttpStatus.NOT_FOUND, exception.getMessage()); }
    @ExceptionHandler({CreateAccount.AccountAlreadyExistsException.class, AuthService.EmailAlreadyRegisteredException.class})
    ProblemDetail conflict(RuntimeException exception) { return problem(HttpStatus.CONFLICT, exception.getMessage()); }
    @ExceptionHandler(AuthService.InvalidCredentialsException.class)
    ProblemDetail invalidCredentials(RuntimeException exception) { return problem(HttpStatus.UNAUTHORIZED, exception.getMessage()); }
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail forbidden(RuntimeException exception) { return problem(HttpStatus.FORBIDDEN, exception.getMessage()); }
    private static ProblemDetail problem(HttpStatus status, String detail) { return ProblemDetail.forStatusAndDetail(status, detail); }
}
