package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.Conversion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ConversionDAO extends BaseDAO {

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
}
