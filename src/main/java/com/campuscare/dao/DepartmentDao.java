package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Department;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {
    public List<Department> findAll() {
        List<Department> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT department_id, department_name, description, status FROM departments ORDER BY department_name")) {
            while (rs.next())
                out.add(new Department(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)));
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }

    public void insert(String name, String description) {
        exec("INSERT INTO departments (department_name, description) VALUES (?, ?)", name, description);
    }

    public void update(int id, String name, String description) {
        exec("UPDATE departments SET department_name = ?, description = ? WHERE department_id = ?", name, description, id);
    }

    public void updateStatus(int id, String status) {
        exec("UPDATE departments SET status = ? WHERE department_id = ?", status, id);
    }

    private void exec(String sql, Object... params) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("UNIQUE"))
                throw new AppException("A department with this name already exists.");
            throw AppException.db(e);
        }
    }
}
