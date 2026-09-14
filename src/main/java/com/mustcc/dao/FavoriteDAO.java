package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.Currency;
import com.mustcc.model.CurrencyPair;
import com.mustcc.model.Favorite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDAO extends BaseDAO {

    private final CurrencyDAO currencyDAO = new CurrencyDAO();

    /** Uses INSERT IGNORE — favorites.uq_user_pair means re-adding an existing
     *  favorite is a harmless no-op rather than a constraint-violation error. */
    public void add(int userId, int pairId) {
        String sql = "INSERT IGNORE INTO favorites (user_id, pair_id) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, pairId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to add favorite pair_id=" + pairId + " for user_id=" + userId, e);
        }
    }

    public void remove(int userId, int pairId) {
        String sql = "DELETE FROM favorites WHERE user_id = ? AND pair_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, pairId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to remove favorite pair_id=" + pairId + " for user_id=" + userId, e);
        }
    }

    public List<Favorite> listForUser(int userId) {
        String sql = "SELECT f.favorite_id, f.added_at, p.pair_id, p.is_active, " +
                "p.base_currency_id, p.target_currency_id " +
                "FROM favorites f JOIN currency_pairs p ON f.pair_id = p.pair_id " +
                "WHERE f.user_id = ? ORDER BY f.added_at";
        List<Favorite> favorites = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Currency base = currencyDAO.findById(rs.getInt("base_currency_id")).orElseThrow();
                    Currency target = currencyDAO.findById(rs.getInt("target_currency_id")).orElseThrow();
                    CurrencyPair pair = new CurrencyPair(rs.getInt("pair_id"), base, target, rs.getBoolean("is_active"));
                    favorites.add(new Favorite(
                            rs.getInt("favorite_id"), pair, rs.getTimestamp("added_at").toLocalDateTime()));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to list favorites for user_id=" + userId, e);
        }
        return favorites;
    }
}
