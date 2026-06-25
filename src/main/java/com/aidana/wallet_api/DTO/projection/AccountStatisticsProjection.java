package com.aidana.wallet_api.DTO.projection;

import java.math.BigDecimal;

public interface AccountStatisticsProjection {

    BigDecimal getTotalDeposits();

    BigDecimal getTotalWithdrawals();

    BigDecimal getTotalTransfers();

    int getTransactionCount();
}
