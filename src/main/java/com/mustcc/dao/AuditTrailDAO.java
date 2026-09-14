package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

/** Write-only log of who changed what. userId may be null for guest activity. */
public class AuditTrailDAO extends BaseDAO {

    public void log(Integer userId, String action, String tableAffected, Integer recordId) {
        String sql = "INSERT INTO audit_trail (user_id, action, table_affected, record_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, tableAffected);
            if (recordId != null) {
                stmt.setInt(4, recordId);
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            // Audit logging failing shouldn't blow up the caller's real work —
            // log to stderr and swallow, rather than throwing DataAccessException.
            System.err.println("Warning: failed to write audit_trail entry: " + e.getMessage());
        }
    }
}
