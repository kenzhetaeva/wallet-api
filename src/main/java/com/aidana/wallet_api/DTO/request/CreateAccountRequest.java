package com.aidana.wallet_api.DTO.request;

import com.aidana.wallet_api.enums.Currency;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAccountRequest {
    private Currency currency;
}
