package com.aidana.wallet_api.DTO.projection;

import java.math.BigDecimal;

public interface TopUserProjection {

    Long getUserId();

    String getEmail();

    BigDecimal getTotalTransferred();
}
