package com.aidana.wallet_api.DTO.response;

import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.enums.TransactionStatus;
import com.aidana.wallet_api.enums.TransactionType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Getter
public class TransactionResponse {

    private final Long id;
    private final Long fromAccountId;
    private final Long toAccountId;
    private final BigDecimal amount;
    private final TransactionStatus status;
    private final TransactionType type;
    private final Instant createdAt;

    public TransactionResponse(Transaction transaction) {
        this.id = transaction.getId();
        this.fromAccountId = Optional.ofNullable(transaction.getFromAccount())
                .map(Account::getId)
                .orElse(null);
        this.toAccountId = Optional.ofNullable(transaction.getToAccount())
                .map(Account::getId)
                .orElse(null);
        this.amount = transaction.getAmount();
        this.status = transaction.getStatus();
        this.type = transaction.getType();
        this.createdAt = transaction.getCreatedAt();
    }
}
