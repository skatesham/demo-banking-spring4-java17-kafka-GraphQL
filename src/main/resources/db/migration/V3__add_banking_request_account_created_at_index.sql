CREATE INDEX banking_request_account_id_created_at_idx
    ON banking_request (account_id, created_at DESC);
