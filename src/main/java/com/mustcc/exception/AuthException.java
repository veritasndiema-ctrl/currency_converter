package com.mustcc.exception;

/** Thrown on registration conflicts (duplicate username/email) or failed login. */
public class AuthException extends Exception {
    public AuthException(String message) {
        super(message);
    }
}
