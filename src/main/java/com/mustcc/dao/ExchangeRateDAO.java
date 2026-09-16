package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.Currency;
import com.mustcc.model.ExchangeRate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ExchangeRateDAO extends BaseDAO {

    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    /** Latest known rate for a base -> target pair. */
    public Optional<ExchangeRate> findLatestRate(int baseCurrencyId, int targetCurrencyId) {
        String sql = "SELECT * FROM exchange_rates " +
                "WHERE base_currency_id = ? AND target_currency_id = ? " +
                "ORDER BY rate_date DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, baseCurrencyId);
            stmt.setInt(2, targetCurrencyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                int rateId = rs.getInt("rate_id");
                int sourceIdRaw = rs.getInt("source_id");
                Integer sourceId = rs.wasNull() ? null : sourceIdRaw;
                var rate = rs.getBigDecimal("rate");
                var rateDate = rs.getDate("rate_date").toLocalDate();

                Currency base = currencyDAO.findById(baseCurrencyId)
                        .orElseThrow(() -> new DataAccessException("Base currency not found: " + baseCurrencyId));
                Currency target = currencyDAO.findById(targetCurrencyId)
                        .orElseThrow(() -> new DataAccessException("Target currency not found: " + targetCurrencyId));

                return Optional.of(new ExchangeRate(
                        rateId, base, target, sourceId, rate, rateDate
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException(
                    "Failed to fetch rate for pair " + baseCurrencyId + "->" + targetCurrencyId, e);
        }
    }
}
