package com.mustcc.dao;

import com.mustcc.util.DatabaseConnection;

import java.sql.Connection;

/**
 * Shared base for all DAOs. Every concrete DAO extends this to reuse
 * connection access instead of duplicating it — the inheritance
 * counterpart to the Repository interface's polymorphism.
 */
public abstract class BaseDAO {
    protected Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }
}
