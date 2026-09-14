package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionLogDAO extends BaseDAO {

    public void log(int conversionId, String status, String message) {
        String sql = "INSERT INTO transaction_log (conversion_id, status, message) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, conversionId);
            stmt.setString(2, status);
            stmt.setString(3, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to write transaction_log for conversion_id=" + conversionId, e);
        }
    }
}
