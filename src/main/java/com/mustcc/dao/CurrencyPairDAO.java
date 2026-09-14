package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.Currency;
import com.mustcc.model.CurrencyPair;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class CurrencyPairDAO extends BaseDAO {

    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    public Optional<CurrencyPair> find(int baseId, int targetId) {
        String sql = "SELECT * FROM currency_pairs WHERE base_currency_id = ? AND target_currency_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, baseId);
            stmt.setInt(2, targetId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch pair " + baseId + "->" + targetId, e);
        }
    }

    /** Returns the existing pair for (baseId, targetId), inserting it first if needed. */
    public CurrencyPair findOrCreate(int baseId, int targetId) {
        return find(baseId, targetId).orElseGet(() -> {
            String sql = "INSERT INTO currency_pairs (base_currency_id, target_currency_id) VALUES (?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, baseId);
                stmt.setInt(2, targetId);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    keys.next();
                    Currency base = currencyDAO.findById(baseId).orElseThrow();
                    Currency target = currencyDAO.findById(targetId).orElseThrow();
                    return new CurrencyPair(keys.getInt(1), base, target, true);
                }
            } catch (SQLException e) {
                throw new DataAccessException("Failed to create pair " + baseId + "->" + targetId, e);
            }
        });
    }

    private CurrencyPair mapRow(ResultSet rs) throws SQLException {
        int baseId = rs.getInt("base_currency_id");
        int targetId = rs.getInt("target_currency_id");
        Currency base = currencyDAO.findById(baseId).orElseThrow();
        Currency target = currencyDAO.findById(targetId).orElseThrow();
        return new CurrencyPair(rs.getInt("pair_id"), base, target, rs.getBoolean("is_active"));
    }
}
