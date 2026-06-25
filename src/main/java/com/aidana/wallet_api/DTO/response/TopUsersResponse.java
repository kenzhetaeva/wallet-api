package com.aidana.wallet_api.DTO.response;

import com.aidana.wallet_api.DTO.projection.TopUserProjection;
import com.aidana.wallet_api.enums.Currency;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
public class TopUsersResponse {
    private final Currency currency;
    private final Instant from;
    private final Instant to;
    private final List<TopUserProjection> users;

    public TopUsersResponse(Currency currency, Instant from, Instant to, List<TopUserProjection> users) {
        this.currency = currency;
        this.from = from;
        this.to = to;
        this.users = users;
    }
}
