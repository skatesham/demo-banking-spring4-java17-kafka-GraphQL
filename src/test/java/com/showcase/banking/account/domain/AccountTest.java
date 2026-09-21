package com.showcase.banking.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AccountTest {

    @Test
    void creditsAndDebitsAnActiveAccount() {
        Account account = Account.create(UUID.randomUUID());

        account.credit(new BigDecimal("100.00"));
        account.debit(new BigDecimal("35.50"));

        assertThat(account.getBalance()).isEqualByComparingTo("64.50");
    }

    @Test
    void rejectsWithdrawalWhenBalanceIsInsufficient() {
        Account account = Account.create(UUID.randomUUID());
        account.credit(new BigDecimal("10.00"));

        assertThatThrownBy(() -> account.debit(new BigDecimal("10.01")))
                .isInstanceOf(InsufficientBalanceException.class);
        assertThat(account.getBalance()).isEqualByComparingTo("10.00");
    }

    @Test
    void rejectsWithdrawalFromInactiveAccount() {
        Account account = Account.create(UUID.randomUUID());
        account.deactivate();

        assertThatThrownBy(() -> account.debit(BigDecimal.ONE))
                .isInstanceOf(AccountInactiveException.class);
    }

    @Test
    void rejectsNonPositiveAmounts() {
        Account account = Account.create(UUID.randomUUID());

        assertThatIllegalArgumentException().isThrownBy(() -> account.credit(BigDecimal.ZERO));
        assertThatIllegalArgumentException().isThrownBy(() -> account.debit(new BigDecimal("-1")));
    }
}
