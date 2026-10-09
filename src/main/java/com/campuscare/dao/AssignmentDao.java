package com.campuscare.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** staff_assignments. All methods run inside the caller's transaction. */
public class AssignmentDao {
    /** Marks old assignments of the complaint as REASSIGNED (history of who had it is kept). */
    public void deactivateAll(Connection c, int complaintId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE staff_assignments SET status = 'REASSIGNED' WHERE complaint_id = ? AND status = 'ACTIVE'")) {
            ps.setInt(1, complaintId);
            ps.executeUpdate();
        }
    }

    public void insert(Connection c, int complaintId, int staffId, Integer assignedBy) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO staff_assignments (complaint_id, staff_id, assigned_by, status) VALUES (?, ?, ?, 'ACTIVE')")) {
            ps.setInt(1, complaintId);
            ps.setInt(2, staffId);
            if (assignedBy == null) ps.setNull(3, java.sql.Types.INTEGER); else ps.setInt(3, assignedBy);
            ps.executeUpdate();
        }
    }

    public Optional<Integer> findActiveStaffId(Connection c, int complaintId) throws SQLException {
        return one(c, "SELECT staff_id FROM staff_assignments WHERE complaint_id = ? AND status = 'ACTIVE' "
                + "ORDER BY assignment_id DESC LIMIT 1", complaintId);
    }

    /** Last staff who ever worked on the complaint (used when a complaint is reopened). */
    public Optional<Integer> findLastStaffId(Connection c, int complaintId) throws SQLException {
        return one(c, "SELECT a.staff_id FROM staff_assignments a JOIN users u ON u.user_id = a.staff_id "
                + "WHERE a.complaint_id = ? AND u.status = 'ACTIVE' ORDER BY a.assignment_id DESC LIMIT 1", complaintId);
    }

    /** Active staff of the department who currently has the fewest open complaints. */
    public Optional<Integer> findLeastLoadedStaff(Connection c, int departmentId) throws SQLException {
        return one(c, "SELECT u.user_id FROM users u WHERE u.role = 'STAFF' AND u.status = 'ACTIVE' "
                + "AND u.department_id = ? ORDER BY (SELECT COUNT(*) FROM staff_assignments a "
                + "JOIN complaints cm ON cm.complaint_id = a.complaint_id WHERE a.staff_id = u.user_id "
                + "AND a.status = 'ACTIVE' AND cm.status IN ('ASSIGNED','ACCEPTED','IN_PROGRESS')) ASC, u.user_id ASC LIMIT 1",
                departmentId);
    }

    private Optional<Integer> one(Connection c, String sql, int param) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getInt(1)) : Optional.empty();
            }
        }
    }
}
