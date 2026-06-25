package com.aidana.wallet_api.DTO.response;

import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.enums.Currency;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
public class AccountResponse {
    private final Long id;
    private final Long userId;
    private final Currency currency;
    private final BigDecimal balance;
    private final boolean isBlocked;
    private final Instant createdAt;

    public AccountResponse(Account account) {
        this.id = account.getId();
        this.userId = account.getUser().getId();
        this.currency = account.getCurrency();
        this.balance = account.getBalance();
        this.isBlocked = account.getBlockedAt() != null;
        this.createdAt = account.getCreatedAt();
    }
}
