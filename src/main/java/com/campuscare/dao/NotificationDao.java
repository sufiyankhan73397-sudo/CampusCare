package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Notification;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    public void insert(Connection c, int userId, Integer complaintId, String message) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO notifications (user_id, complaint_id, message) VALUES (?, ?, ?)")) {
            ps.setInt(1, userId);
            if (complaintId == null) ps.setNull(2, java.sql.Types.INTEGER); else ps.setInt(2, complaintId);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }

    public List<Notification> findByUser(int userId) {
        List<Notification> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT notification_id, complaint_id, message, is_read, created_at FROM notifications "
                     + "WHERE user_id = ? ORDER BY notification_id DESC LIMIT 200")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int cid = rs.getInt(2);
                    Integer complaintId = rs.wasNull() ? null : cid;
                    out.add(new Notification(rs.getInt(1), complaintId, rs.getString(3), rs.getInt(4) == 1, rs.getString(5)));
                }
            }
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }

    public int countUnread(int userId) {
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw AppException.db(e); }
    }

    public void markRead(int notificationId, int userId) {
        exec("UPDATE notifications SET is_read = 1 WHERE notification_id = ? AND user_id = ?", notificationId, userId);
    }

    public void markAllRead(int userId) {
        exec("UPDATE notifications SET is_read = 1 WHERE user_id = ?", userId);
    }

    private void exec(String sql, Object... params) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { throw AppException.db(e); }
    }
}
