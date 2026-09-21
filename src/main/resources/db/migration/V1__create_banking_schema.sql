CREATE TABLE account (
    id UUID PRIMARY KEY,
    holder_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT account_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT account_balance_non_negative CHECK (balance >= 0)
);

CREATE TABLE banking_request (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES account (id),
    operation_type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE,
    rejection_reason VARCHAR(500),
    CONSTRAINT banking_request_operation_type_valid CHECK (operation_type IN ('DEPOSIT', 'WITHDRAWAL')),
    CONSTRAINT banking_request_status_valid CHECK (status IN ('PENDING', 'COMPLETED', 'REJECTED')),
    CONSTRAINT banking_request_amount_positive CHECK (amount > 0),
    CONSTRAINT banking_request_final_state_consistent CHECK (
        (status = 'PENDING' AND processed_at IS NULL AND rejection_reason IS NULL)
        OR (status = 'COMPLETED' AND processed_at IS NOT NULL AND rejection_reason IS NULL)
        OR (status = 'REJECTED' AND processed_at IS NOT NULL AND rejection_reason IS NOT NULL)
    )
);

CREATE TABLE banking_transaction (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE REFERENCES banking_request (id),
    account_id UUID NOT NULL REFERENCES account (id),
    operation_type VARCHAR(20) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    balance_after NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT banking_transaction_operation_type_valid CHECK (operation_type IN ('DEPOSIT', 'WITHDRAWAL')),
    CONSTRAINT banking_transaction_amount_positive CHECK (amount > 0),
    CONSTRAINT banking_transaction_balance_non_negative CHECK (balance_after >= 0)
);

CREATE INDEX banking_request_account_id_idx ON banking_request (account_id);
CREATE INDEX banking_transaction_account_id_created_at_idx ON banking_transaction (account_id, created_at DESC);
