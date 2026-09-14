package com.mustcc.exception;

/** Thrown when a currency code doesn't exist or is inactive. */
public class InvalidCurrencyException extends Exception {
    public InvalidCurrencyException(String currencyCode) {
        super("Invalid or unknown currency code: " + currencyCode);
    }
}
