package com.aidana.wallet_api.exception;

public class InvalidAccountsException extends RuntimeException {
    public InvalidAccountsException(String message) {
        super(message);
    }
}
