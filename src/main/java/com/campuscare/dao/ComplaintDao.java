package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Complaint;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComplaintDao {
    /** Joins everything so the UI always receives names, never raw ids. */
    private static final String BASE =
            "SELECT c.complaint_id, c.user_id, u.name, c.category_id, cat.category_name, cat.department_id, "
            + "d.department_name, c.location_id, l.location_name, c.title, c.description, c.priority, c.status, "
            + "c.created_at, c.updated_at, c.resolved_at, "
            + "(SELECT s.name FROM staff_assignments a JOIN users s ON s.user_id = a.staff_id "
            + " WHERE a.complaint_id = c.complaint_id AND a.status = 'ACTIVE' ORDER BY a.assignment_id DESC LIMIT 1) "
            + "FROM complaints c "
            + "JOIN users u ON u.user_id = c.user_id "
            + "JOIN categories cat ON cat.category_id = c.category_id "
            + "JOIN departments d ON d.department_id = cat.department_id "
            + "JOIN locations l ON l.location_id = c.location_id ";

    private static final String ORDER = " ORDER BY c.created_at DESC, c.complaint_id DESC";

    private Complaint map(ResultSet rs) throws SQLException {
        return new Complaint(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4), rs.getString(5),
                rs.getInt(6), rs.getString(7), rs.getInt(8), rs.getString(9), rs.getString(10),
                rs.getString(11), rs.getString(12), rs.getString(13), rs.getString(14), rs.getString(15),
                rs.getString(16), rs.getString(17));
    }

    // The WHERE parts below are fixed strings written here (never user input); values are always bound.
    public List<Complaint> findAll() {
        return query("", new Object[0]);
    }

    public List<Complaint> findByUser(int userId) {
        return query("WHERE c.user_id = ?", new Object[]{userId});
    }

    public List<Complaint> findByStaff(int staffId) {
        return query("WHERE EXISTS (SELECT 1 FROM staff_assignments a WHERE a.complaint_id = c.complaint_id "
                + "AND a.staff_id = ? AND a.status = 'ACTIVE')", new Object[]{staffId});
    }

    private List<Complaint> query(String where, Object[] params) {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(BASE + where + ORDER)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            List<Complaint> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) { throw AppException.db(e); }
    }

    public Optional<Complaint> findById(int id) {
        try (Connection c = DatabaseConnection.get()) {
            return findById(c, id);
        } catch (SQLException e) { throw AppException.db(e); }
    }

    public Optional<Complaint> findById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(BASE + "WHERE c.complaint_id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insert(Connection c, int userId, int categoryId, int locationId,
                      String title, String description, String priority) throws SQLException {
        String sql = "INSERT INTO complaints (user_id, category_id, location_id, title, description, priority, status) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'SUBMITTED')";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, categoryId);
            ps.setInt(3, locationId);
            ps.setString(4, title);
            ps.setString(5, description);
            ps.setString(6, priority);
            ps.executeUpdate();
        }
        return DatabaseConnection.lastInsertId(c);
    }

    /** Updates status + updated_at (and resolved_at when needed). */
    public void updateStatus(Connection c, int complaintId, String newStatus) throws SQLException {
        String sql = "UPDATE complaints SET status = ?, updated_at = datetime('now','localtime'), "
                + "resolved_at = CASE WHEN ? = 'RESOLVED' THEN datetime('now','localtime') "
                + "WHEN ? = 'REOPENED' THEN NULL ELSE resolved_at END WHERE complaint_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, newStatus);
            ps.setString(3, newStatus);
            ps.setInt(4, complaintId);
            ps.executeUpdate();
        }
    }
}
