package com.mustcc.dao;

import com.mustcc.exception.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** Looks up role_id by role_name (ADMIN / USER). Small enough that it
 *  doesn't need the full Repository<T,ID> contract. */
public class RoleDAO extends BaseDAO {

    public Optional<Integer> findIdByName(String roleName) {
        String sql = "SELECT role_id FROM roles WHERE role_name = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt("role_id")) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch role id for " + roleName, e);
        }
    }
}
