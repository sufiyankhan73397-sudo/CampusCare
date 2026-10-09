package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Category;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CategoryDao {
    public List<Category> findAll() {
        List<Category> out = new ArrayList<>();
        String sql = "SELECT c.category_id, c.category_name, c.department_id, d.department_name "
                + "FROM categories c JOIN departments d ON d.department_id = c.department_id ORDER BY c.category_name";
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next())
                out.add(new Category(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)));
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }

    public void insert(String name, int departmentId) {
        exec("INSERT INTO categories (category_name, department_id) VALUES (?, ?)", name, departmentId);
    }

    public void update(int id, String name, int departmentId) {
        exec("UPDATE categories SET category_name = ?, department_id = ? WHERE category_id = ?", name, departmentId, id);
    }

    public void delete(int id) {
        exec("DELETE FROM categories WHERE category_id = ?", id);
    }

    private void exec(String sql, Object... params) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            String m = e.getMessage() == null ? "" : e.getMessage();
            if (m.contains("UNIQUE")) throw new AppException("A category with this name already exists.");
            if (m.toLowerCase().contains("foreign key")) throw new AppException("This category is used by complaints and cannot be deleted.");
            throw AppException.db(e);
        }
    }
}
