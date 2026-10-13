package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Attachment;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttachmentDao {
    public void insert(Connection c, int complaintId, String fileName, String filePath, int uploadedBy)
            throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO attachments (complaint_id, file_name, file_path, uploaded_by) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, complaintId);
            ps.setString(2, fileName);
            ps.setString(3, filePath);
            ps.setInt(4, uploadedBy);
            ps.executeUpdate();
        }
    }

    public List<Attachment> findByComplaint(int complaintId) {
        List<Attachment> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT attachment_id, file_name, file_path FROM attachments WHERE complaint_id = ?")) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new Attachment(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }
}
