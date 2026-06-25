package com.aidana.wallet_api.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException() {
        super("Currency of source account and destination account must be the same");
    }
}
