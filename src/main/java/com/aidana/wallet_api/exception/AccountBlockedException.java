package com.aidana.wallet_api.exception;

public class AccountBlockedException extends RuntimeException {
    public AccountBlockedException() {
        super("Account is blocked");
    }
}
