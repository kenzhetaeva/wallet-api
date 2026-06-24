package com.aidana.wallet_api.DTO.response;

import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.enums.Currency;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class AccountResponse {
    private final Long id;
    private final Currency currency;
    private final BigDecimal balance;

    public AccountResponse(Account account) {
        this.id = account.getId();
        this.currency = account.getCurrency();
        this.balance = account.getBalance();
    }
}
