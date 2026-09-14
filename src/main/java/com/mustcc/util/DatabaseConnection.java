package com.mustcc.util;

import com.mustcc.exception.DataAccessException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton wrapper around a single JDBC Connection.
 *
 * Credentials are no longer hard-coded here. They're loaded from
 * db.properties on the classpath (src/main/resources/db.properties),
 * which is gitignored — only db.properties.example (a template with
 * placeholder values) is committed. Copy the example, rename it, and
 * fill in your own values before running.
 *
 * For a coursework project a single shared connection is fine; if you
 * want to demonstrate more advanced resource management, swap this
 * implementation for a HikariCP connection pool without changing any
 * DAO code — that's the point of hiding it behind getConnection().
 */
public final class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConnection.class.getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new DataAccessException(
                        "Missing " + CONFIG_FILE + " on the classpath. Copy " +
                        "src/main/resources/db.properties.example to " +
                        "src/main/resources/db.properties and fill in your " +
                        "own database credentials.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new DataAccessException("Failed to read " + CONFIG_FILE, e);
        }

        URL = require(props, "db.url");
        USER = require(props, "db.user");
        PASSWORD = require(props, "db.password");
    }

    private static String require(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new DataAccessException("Missing required property '" + key + "' in db.properties");
        }
        return value;
    }

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
