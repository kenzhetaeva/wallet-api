package com.aidana.wallet_api.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountStatisticsResponse {

    private final BigDecimal totalDeposits;
    private final BigDecimal totalWithdrawals;
    private final BigDecimal totalTransfers;
    private final int transactionCount;
}
