package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.HistoryEntry;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** complaint_history: tracking + audit trail. Every status change inserts one row here. */
public class HistoryDao {
    public void insert(Connection c, int complaintId, Integer updatedBy, String oldStatus,
                       String newStatus, String remarks) throws SQLException {
        String sql = "INSERT INTO complaint_history (complaint_id, updated_by, old_status, new_status, remarks) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            if (updatedBy == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, updatedBy);
            ps.setString(3, oldStatus);   // NULL for the first row
            ps.setString(4, newStatus);
            ps.setString(5, remarks);
            ps.executeUpdate();
        }
    }

    public List<HistoryEntry> findByComplaint(int complaintId) {
        String sql = "SELECT COALESCE(u.name, 'System'), h.old_status, h.new_status, h.remarks, h.updated_at "
                + "FROM complaint_history h LEFT JOIN users u ON u.user_id = h.updated_by "
                + "WHERE h.complaint_id = ? ORDER BY h.history_id";
        List<HistoryEntry> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(new HistoryEntry(rs.getString(1), rs.getString(2), rs.getString(3),
                            rs.getString(4), rs.getString(5)));
            }
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }
}
