package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;
import com.mustcc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO extends BaseDAO implements Repository<User, Integer> {

    @Override
    public Optional<User> findById(Integer id) {
        return findByColumn("user_id", id);
    }

    public Optional<User> findByUsername(String username) {
        return findByColumn("username", username);
    }

    public Optional<User> findByEmail(String email) {
        return findByColumn("email", email);
    }

    private Optional<User> findByColumn(String column, Object value) {
        String sql = "SELECT u.*, r.role_name FROM users u " +
                "JOIN roles r ON u.role_id = r.role_id WHERE u." + column + " = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, value);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch user by " + column + "=" + value, e);
        }
    }

    @Override
    public List<User> findAll() {
        String sql = "SELECT u.*, r.role_name FROM users u JOIN roles r ON u.role_id = r.role_id " +
                "ORDER BY u.username";
        List<User> users = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch users", e);
        }
        return users;
    }

    /**
     * Inserts a new user. Expects passwordHash to already be a bcrypt hash
     * (AuthService is responsible for hashing — the DAO never sees a raw
     * password) and roleId to be resolved via RoleDAO beforehand.
     */
    public User save(User user, int roleId) {
        String sql = "INSERT INTO users (username, email, password_hash, role_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, roleId);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                int newId = keys.next() ? keys.getInt(1) : user.getUserId();
                return new User(newId, user.getUsername(), user.getEmail(),
                        user.getPasswordHash(), user.getRole());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save user " + user, e);
        }
    }

    @Override
    public User save(User user) {
        throw new UnsupportedOperationException(
                "Use save(User, int roleId) — role assignment is required for a new user");
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete user id=" + id, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("email"),
                rs.getString("password_hash"),
                User.Role.valueOf(rs.getString("role_name"))
        );
    }
}
