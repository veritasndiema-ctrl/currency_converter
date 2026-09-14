package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.Currency;
import com.mustcc.model.RateHistoryEntry;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RateHistoryDAO extends BaseDAO {

    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    /** Called every time a rate is used, so rate_history builds up an
     *  append-only log even though exchange_rates only keeps one row/day. */
    public void record(int baseId, int targetId, BigDecimal rate) {
        String sql = "INSERT INTO rate_history (base_currency_id, target_currency_id, rate) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, baseId);
            stmt.setInt(2, targetId);
            stmt.setBigDecimal(3, rate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to record rate history for " + baseId + "->" + targetId, e);
        }
    }

    public List<RateHistoryEntry> findRecent(int baseId, int targetId, int days) {
        String sql = "SELECT * FROM rate_history WHERE base_currency_id = ? AND target_currency_id = ? " +
                "AND recorded_at >= (NOW() - INTERVAL ? DAY) ORDER BY recorded_at DESC";
        List<RateHistoryEntry> entries = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, baseId);
            stmt.setInt(2, targetId);
            stmt.setInt(3, days);
            try (ResultSet rs = stmt.executeQuery()) {
                Currency base = currencyDAO.findById(baseId).orElseThrow();
                Currency target = currencyDAO.findById(targetId).orElseThrow();
                while (rs.next()) {
                    entries.add(new RateHistoryEntry(
                            rs.getInt("history_id"), base, target,
                            rs.getBigDecimal("rate"),
                            rs.getTimestamp("recorded_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch rate history for " + baseId + "->" + targetId, e);
        }
        return entries;
    }
}
