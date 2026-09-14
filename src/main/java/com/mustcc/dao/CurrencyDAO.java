package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.Currency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDAO extends BaseDAO implements Repository<Currency, Integer> {

    @Override
    public Optional<Currency> findById(Integer id) {
        String sql = "SELECT * FROM currencies WHERE currency_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch currency id=" + id, e);
        }
    }

    public Optional<Currency> findByCode(String code) {
        String sql = "SELECT * FROM currencies WHERE currency_code = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch currency code=" + code, e);
        }
    }

    @Override
    public List<Currency> findAll() {
        String sql = "SELECT * FROM currencies ORDER BY currency_code";
        List<Currency> currencies = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                currencies.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch currencies", e);
        }
        return currencies;
    }

    @Override
    public Currency save(Currency currency) {
        String sql = "INSERT INTO currencies (currency_code, currency_name, symbol) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, currency.getCurrencyCode());
            stmt.setString(2, currency.getCurrencyName());
            stmt.setString(3, currency.getSymbol());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                int newId = keys.next() ? keys.getInt(1) : currency.getCurrencyId();
                return new Currency(newId, currency.getCurrencyCode(),
                        currency.getCurrencyName(), currency.getSymbol());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save currency " + currency, e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM currencies WHERE currency_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete currency id=" + id, e);
        }
    }

    private Currency mapRow(ResultSet rs) throws SQLException {
        return new Currency(
                rs.getInt("currency_id"),
                rs.getString("currency_code"),
                rs.getString("currency_name"),
                rs.getString("symbol")
        );
    }
}
