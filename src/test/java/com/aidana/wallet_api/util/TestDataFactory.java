package com.aidana.wallet_api.util;

import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.RefreshToken;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.enums.Role;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public final class TestDataFactory {

    public static User createUser() {
        User user = new User();
        user.setFirstName("FirstName");
        user.setLastName("LastName");
        user.setEmail("email@mail.com");
        user.setRole(Role.USER);
        user.setPassword("password");

        return user;
    }

    public static RefreshToken createRefreshToken(User user, String hashedToken) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashedToken);
        refreshToken.setCreatedAt(Instant.now());
        refreshToken.setExpiresAt(Instant.now().plus(1, ChronoUnit.DAYS));

        return refreshToken;
    }

    public static Account createAccount(User user, Currency currency) {
        Account account = new Account();
        account.setUser(user);
        account.setCurrency(currency);
        account.setCreatedAt(Instant.now());

        return account;
    }
}
