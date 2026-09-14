package com.mustcc.exception;

/** Thrown when no exchange rate exists for a requested currency pair. */
public class RateUnavailableException extends Exception {
    public RateUnavailableException(String base, String target) {
        super("No exchange rate available for " + base + " -> " + target);
    }
}
