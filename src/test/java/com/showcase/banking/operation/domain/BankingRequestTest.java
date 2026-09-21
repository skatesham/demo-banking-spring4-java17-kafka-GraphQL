package com.showcase.banking.operation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class BankingRequestTest {

    @Test
    void completesOnlyOnce() {
        BankingRequest request = BankingRequest.pending(UUID.randomUUID(), BankingOperationType.DEPOSIT,
                new BigDecimal("25.00"));

        request.complete();

        assertThat(request.getStatus()).isEqualTo(BankingRequestStatus.COMPLETED);
        assertThat(request.getProcessedAt()).isNotNull();
        assertThatIllegalStateException().isThrownBy(request::complete);
    }

    @Test
    void recordsReasonWhenRejected() {
        BankingRequest request = BankingRequest.pending(UUID.randomUUID(), BankingOperationType.WITHDRAWAL,
                new BigDecimal("25.00"));

        request.reject("Insufficient balance");

        assertThat(request.getStatus()).isEqualTo(BankingRequestStatus.REJECTED);
        assertThat(request.getRejectionReason()).isEqualTo("Insufficient balance");
    }
}
