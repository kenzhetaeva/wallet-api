package com.aidana.wallet_api.DTO.request;

import com.aidana.wallet_api.enums.Currency;
import lombok.Getter;

@Getter
public class CreateAccountRequest {
    private Currency currency;
}
