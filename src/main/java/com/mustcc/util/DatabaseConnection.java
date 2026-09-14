package com.mustcc.util;

import com.mustcc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton wrapper around a single JDBC Connection.
 *
 * For a coursework project a single shared connection is fine; if you
 * want to demonstrate more advanced resource management, swap this
 * implementation for a HikariCP connection pool without changing any
 * DAO code — that's the point of hiding it behind getConnection().
 */
public final class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/currency_converter?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "your_password_here";

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new DataAccessException("Failed to connect to database", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to re-establish database connection", e);
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to close database connection", e);
        }
    }
}
