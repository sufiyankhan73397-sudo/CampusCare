package com.campuscare.dao;

import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Location;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class LocationDao {
    public List<Location> findAll() {
        List<Location> out = new ArrayList<>();
        try (Connection c = DatabaseConnection.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT location_id, location_name, building, floor, description FROM locations ORDER BY location_name")) {
            while (rs.next())
                out.add(new Location(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
        } catch (SQLException e) { throw AppException.db(e); }
        return out;
    }

    public void insert(String name, String building, String floor, String description) {
        exec("INSERT INTO locations (location_name, building, floor, description) VALUES (?, ?, ?, ?)",
                name, building, floor, description);
    }

    public void update(int id, String name, String building, String floor, String description) {
        exec("UPDATE locations SET location_name = ?, building = ?, floor = ?, description = ? WHERE location_id = ?",
                name, building, floor, description, id);
    }

    public void delete(int id) {
        exec("DELETE FROM locations WHERE location_id = ?", id);
    }

    private void exec(String sql, Object... params) {
        try (Connection c = DatabaseConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) {
            String m = e.getMessage() == null ? "" : e.getMessage();
            if (m.contains("UNIQUE")) throw new AppException("A location with this name already exists.");
            if (m.toLowerCase().contains("foreign key")) throw new AppException("This location is used by complaints and cannot be deleted.");
            throw AppException.db(e);
        }
    }
}
