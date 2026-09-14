package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.Conversion;
import com.mustcc.model.Currency;
import com.mustcc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ConversionDAO extends BaseDAO {

    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    public Conversion save(Conversion conversion) {
        String sql = "INSERT INTO conversions " +
                "(user_id, base_currency_id, target_currency_id, amount, converted_amount, rate_used) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (conversion.getUser() != null) {
                stmt.setInt(1, conversion.getUser().getUserId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setInt(2, conversion.getBase().getCurrencyId());
            stmt.setInt(3, conversion.getTarget().getCurrencyId());
            stmt.setBigDecimal(4, conversion.getAmount());
            stmt.setBigDecimal(5, conversion.getConvertedAmount());
            stmt.setBigDecimal(6, conversion.getRateUsed());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    conversion.setConversionId(keys.getInt(1));
                }
            }
            return conversion;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save conversion " + conversion, e);
        }
    }

    /** Most recent conversions for a logged-in user — backs the CLI's
     *  "my conversion history" menu option. Guest conversions (user_id
     *  NULL) are never returned here since they belong to no one. */
    public List<Conversion> findByUser(int userId, int limit) {
        String sql = "SELECT * FROM conversions WHERE user_id = ? ORDER BY conversion_date DESC LIMIT ?";
        List<Conversion> results = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch conversions for user_id=" + userId, e);
        }
        return results;
    }

    private Conversion mapRow(ResultSet rs) throws SQLException {
        Currency base = currencyDAO.findById(rs.getInt("base_currency_id")).orElseThrow();
        Currency target = currencyDAO.findById(rs.getInt("target_currency_id")).orElseThrow();
        // User isn't reloaded here — the caller already knows who they are
        // (this is only ever queried for the current logged-in user).
        Conversion c = new Conversion(null, base, target,
                rs.getBigDecimal("amount"), rs.getBigDecimal("converted_amount"), rs.getBigDecimal("rate_used"));
        c.setConversionId(rs.getInt("conversion_id"));
        return c;
    }
}
