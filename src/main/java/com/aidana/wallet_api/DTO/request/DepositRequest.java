package com.aidana.wallet_api.DTO.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class DepositRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 17, fraction = 2, message = "Amount must have up to 17 integer digits and 2 decimal places")
    private BigDecimal amount;
}
