package com.aidana.wallet_api.exception;

public class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException() {
        super("Balance is less than withdraw amount");
    }
}
