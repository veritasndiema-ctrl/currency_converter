package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ErrorLogDAO extends BaseDAO {

    public void log(Integer userId, String errorMessage, String stackTrace) {
        String sql = "INSERT INTO error_log (user_id, error_message, stack_trace) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, errorMessage);
            stmt.setString(3, stackTrace);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Warning: failed to write error_log entry: " + e.getMessage());
        }
    }
}
