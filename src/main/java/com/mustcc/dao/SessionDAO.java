package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SessionDAO extends BaseDAO {

    public int open(int userId, String ipAddress) {
        String sql = "INSERT INTO user_sessions (user_id, ip_address) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setString(2, ipAddress);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to open session for user_id=" + userId, e);
        }
    }

    public void close(int sessionId) {
        String sql = "UPDATE user_sessions SET logout_time = CURRENT_TIMESTAMP WHERE session_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to close session_id=" + sessionId, e);
        }
    }
}
