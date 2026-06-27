package com.aidana.wallet_api.util;

import com.aidana.wallet_api.entity.Account;
import com.aidana.wallet_api.entity.RefreshToken;
import com.aidana.wallet_api.entity.Transaction;
import com.aidana.wallet_api.entity.User;
import com.aidana.wallet_api.enums.Currency;
import com.aidana.wallet_api.enums.Role;
import com.aidana.wallet_api.enums.TransactionStatus;
import com.aidana.wallet_api.enums.TransactionType;

import java.math.BigDecimal;
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
        account.setBalance(BigDecimal.valueOf(1000));
        account.setCreatedAt(Instant.now());

        return account;
    }

    public static Transaction createTransaction(Account account) {
        Transaction transaction = new Transaction();
        transaction.setFromAccount(account);
        transaction.setToAccount(null);
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setCreatedAt(Instant.now());

        return transaction;
    }
}
