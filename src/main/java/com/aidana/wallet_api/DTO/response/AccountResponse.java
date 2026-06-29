package com.aidana.wallet_api.DTO.response;

import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.enums.Currency;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public AccountResponse(
            @JsonProperty("id") Long id,
            @JsonProperty("userId") Long userId,
            @JsonProperty("currency") Currency currency,
            @JsonProperty("balance") BigDecimal balance,
            @JsonProperty("blocked") boolean isBlocked,
            @JsonProperty("createdAt") Instant createdAt) {

        this.id = id;
        this.userId = userId;
        this.currency = currency;
        this.balance = balance;
        this.isBlocked = isBlocked;
        this.createdAt = createdAt;
    }

    public AccountResponse(Account account) {
        this.id = account.getId();
        this.userId = account.getUser().getId();
        this.currency = account.getCurrency();
        this.balance = account.getBalance();
        this.isBlocked = account.getBlockedAt() != null;
        this.createdAt = account.getCreatedAt();
    }
}
