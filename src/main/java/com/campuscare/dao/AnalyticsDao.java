package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/** Read-only aggregate queries for the admin dashboard and charts. */
public class AnalyticsDao {
    private static final String BY_STATUS = "SELECT status, COUNT(*) FROM complaints GROUP BY status";
    private static final String BY_PRIORITY = "SELECT priority, COUNT(*) FROM complaints GROUP BY priority";
    private static final String BY_DEPARTMENT =
            "SELECT d.department_name, COUNT(*) FROM complaints c "
            + "JOIN categories cat ON cat.category_id = c.category_id "
            + "JOIN departments d ON d.department_id = cat.department_id GROUP BY d.department_name ORDER BY 2 DESC";
    private static final String BY_CATEGORY =
            "SELECT cat.category_name, COUNT(*) FROM complaints c "
            + "JOIN categories cat ON cat.category_id = c.category_id GROUP BY cat.category_name ORDER BY 2 DESC";

    public Map<String, Integer> countByStatus() { return group(BY_STATUS); }
    public Map<String, Integer> countByPriority() { return group(BY_PRIORITY); }
    public Map<String, Integer> countByDepartment() { return group(BY_DEPARTMENT); }
    public Map<String, Integer> countByCategory() { return group(BY_CATEGORY); }

    public int totalUsers() { return scalar("SELECT COUNT(*) FROM users"); }
    public int totalStaff() { return scalar("SELECT COUNT(*) FROM users WHERE role = 'STAFF'"); }

    private int scalar(String sql) {
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { throw AppException.db(e); }
    }

    private Map<String, Integer> group(String sql) {
        Map<String, Integer> out = new LinkedHashMap<>();
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) out.put(rs.getString(1), rs.getInt(2));
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }
}
