package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.DepartmentRating;
import com.campuscare.model.FeedbackRow;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FeedbackDao {
    public boolean existsForComplaint(Connection c, int complaintId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM feedback WHERE complaint_id = ?")) {
            ps.setInt(1, complaintId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public void insert(Connection c, int complaintId, int userId, int rating, String comments) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO feedback (complaint_id, user_id, rating, comments) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, complaintId);
            ps.setInt(2, userId);
            ps.setInt(3, rating);
            ps.setString(4, comments);
            ps.executeUpdate();
        }
    }

    public List<FeedbackRow> findAll() {
        String sql = "SELECT f.complaint_id, c.title, u.name, d.department_name, f.rating, f.comments, f.created_at "
                + "FROM feedback f JOIN complaints c ON c.complaint_id = f.complaint_id "
                + "JOIN users u ON u.user_id = f.user_id "
                + "JOIN categories cat ON cat.category_id = c.category_id "
                + "JOIN departments d ON d.department_id = cat.department_id ORDER BY f.feedback_id DESC";
        List<FeedbackRow> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                out.add(new FeedbackRow(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getInt(5), rs.getString(6), rs.getString(7)));
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }

    public List<DepartmentRating> averageByDepartment() {
        String sql = "SELECT d.department_name, AVG(f.rating), COUNT(*) FROM feedback f "
                + "JOIN complaints c ON c.complaint_id = f.complaint_id "
                + "JOIN categories cat ON cat.category_id = c.category_id "
                + "JOIN departments d ON d.department_id = cat.department_id "
                + "GROUP BY d.department_name ORDER BY d.department_name";
        List<DepartmentRating> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.add(new DepartmentRating(rs.getString(1), rs.getDouble(2), rs.getInt(3)));
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }

    /** {average, total} over all feedback. */
    public double[] overall() {
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(AVG(rating), 0), COUNT(*) FROM feedback")) {
            return rs.next() ? new double[]{rs.getDouble(1), rs.getInt(2)} : new double[]{0, 0};
        } catch (SQLException e) { throw AppException.db(e); }
    }
}
