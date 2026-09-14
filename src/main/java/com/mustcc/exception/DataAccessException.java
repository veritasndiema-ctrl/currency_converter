package com.mustcc.exception;

/**
 * Wraps low-level SQLException/JDBC failures so the service and UI
 * layers never need to import java.sql.* directly. Keeps persistence
 * details out of higher layers (a core reason DAO+exception wrapping
 * is used instead of letting SQLException propagate everywhere).
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
