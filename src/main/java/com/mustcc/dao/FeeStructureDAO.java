package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.FeeStructure;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class FeeStructureDAO extends BaseDAO {

    public Optional<FeeStructure> findBySource(int sourceId) {
        String sql = "SELECT * FROM fee_structures WHERE source_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sourceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new FeeStructure(
                        rs.getInt("fee_id"),
                        rs.getInt("source_id"),
                        rs.getBigDecimal("fee_percentage"),
                        rs.getBigDecimal("min_fee"),
                        rs.getBigDecimal("max_fee")));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch fee structure for source_id=" + sourceId, e);
        }
    }
}
