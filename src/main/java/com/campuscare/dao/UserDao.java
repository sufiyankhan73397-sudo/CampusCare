package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.User;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDao {
    private static final String SELECT =
            "SELECT u.user_id, u.name, u.email, u.phone, u.role, u.department_id, d.department_name, "
            + "u.status, u.created_at FROM users u LEFT JOIN departments d ON d.department_id = u.department_id ";

    private User map(ResultSet rs) throws SQLException {
        int dep = rs.getInt(6);
        Integer depId = rs.wasNull() ? null : dep;
        return new User(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5),
                depId, rs.getString(7), rs.getString(8), rs.getString(9));
    }

    public Optional<User> findByEmail(String email) {
        return findOne(SELECT + "WHERE LOWER(u.email) = LOWER(?)", email);
    }

    public Optional<User> findById(int id) {
        return findOne(SELECT + "WHERE u.user_id = ?", id);
    }

    private Optional<User> findOne(String sql, Object param) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw AppException.db(e); }
    }

    /** Only used by login / change-password to verify the BCrypt hash. */
    public Optional<String> findPasswordHashByEmail(String email) {
        return hashQuery("SELECT password FROM users WHERE LOWER(email) = LOWER(?)", email);
    }

    public Optional<String> findPasswordHashById(int id) {
        return hashQuery("SELECT password FROM users WHERE user_id = ?", id);
    }

    private Optional<String> hashQuery(String sql, Object param) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        } catch (SQLException e) { throw AppException.db(e); }
    }

    public boolean emailExists(String email) {
        return findByEmail(email).isPresent();
    }

    public List<User> findAll() {
        return list(SELECT + "ORDER BY u.user_id", null);
    }

    /** Active staff members of one department (used for assignment). */
    public List<User> findActiveStaffByDepartment(int departmentId) {
        return list(SELECT + "WHERE u.role = 'STAFF' AND u.status = 'ACTIVE' AND u.department_id = ? ORDER BY u.name",
                departmentId);
    }

    private List<User> list(String sql, Object param) {
        List<User> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }

    /** Ids of all active users with a role (used to notify admins). Runs inside a transaction. */
    public List<Integer> findActiveIdsByRole(Connection c, String role) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT user_id FROM users WHERE role = ? AND status = 'ACTIVE'")) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    /** Name lookup that runs inside the caller's transaction. */
    public String findNameById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT name FROM users WHERE user_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getString(1) : "Unknown"; }
        }
    }

    public boolean isActiveStaffOfDepartment(Connection c, int staffId, int departmentId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT 1 FROM users WHERE user_id = ? AND role = 'STAFF' AND status = 'ACTIVE' AND department_id = ?")) {
            ps.setInt(1, staffId);
            ps.setInt(2, departmentId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public int insert(String name, String email, String passwordHash, String phone, String role, Integer departmentId) {
        String sql = "INSERT INTO users (name, email, password, phone, role, department_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, phone);
            ps.setString(5, role);
            if (departmentId == null) ps.setNull(6, java.sql.Types.INTEGER); else ps.setInt(6, departmentId);
            ps.executeUpdate();
            return DatabaseConnection.lastInsertId(c);
        } catch (SQLException e) { throw AppException.db(e); }
    }

    public void updateProfile(int id, String name, String phone) {
        exec("UPDATE users SET name = ?, phone = ? WHERE user_id = ?", name, phone, id);
    }

    public void updatePassword(int id, String hash) {
        exec("UPDATE users SET password = ? WHERE user_id = ?", hash, id);
    }

    public void updateStatus(int id, String status) {
        exec("UPDATE users SET status = ? WHERE user_id = ?", status, id);
    }

    private void exec(String sql, Object... params) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { throw AppException.db(e); }
    }
}
