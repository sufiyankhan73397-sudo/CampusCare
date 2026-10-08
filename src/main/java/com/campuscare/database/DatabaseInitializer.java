package com.campuscare.database;

import com.campuscare.config.AppConfig;
import com.campuscare.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Creates the SQLite file, all tables and the default data on first run.
 * Safe to call on every start (uses IF NOT EXISTS and seeds only once).
 */
public final class DatabaseInitializer {
    private DatabaseInitializer() { }

    private static final String[] TABLES = {
        "CREATE TABLE IF NOT EXISTS departments ("
            + "department_id INTEGER PRIMARY KEY AUTOINCREMENT, department_name TEXT NOT NULL UNIQUE, "
            + "description TEXT, status TEXT NOT NULL DEFAULT 'ACTIVE')",

        "CREATE TABLE IF NOT EXISTS users ("
            + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, "
            + "password TEXT NOT NULL, phone TEXT, "
            + "role TEXT NOT NULL CHECK (role IN ('STUDENT','STAFF','ADMIN')), "
            + "department_id INTEGER REFERENCES departments(department_id), "
            + "status TEXT NOT NULL DEFAULT 'ACTIVE', "
            + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))",

        "CREATE TABLE IF NOT EXISTS categories ("
            + "category_id INTEGER PRIMARY KEY AUTOINCREMENT, category_name TEXT NOT NULL UNIQUE, "
            + "department_id INTEGER NOT NULL REFERENCES departments(department_id))",

        "CREATE TABLE IF NOT EXISTS locations ("
            + "location_id INTEGER PRIMARY KEY AUTOINCREMENT, location_name TEXT NOT NULL UNIQUE, "
            + "building TEXT, floor TEXT, description TEXT)",

        "CREATE TABLE IF NOT EXISTS complaints ("
            + "complaint_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "user_id INTEGER NOT NULL REFERENCES users(user_id), "
            + "category_id INTEGER NOT NULL REFERENCES categories(category_id), "
            + "location_id INTEGER NOT NULL REFERENCES locations(location_id), "
            + "title TEXT NOT NULL, description TEXT NOT NULL, "
            + "priority TEXT NOT NULL CHECK (priority IN ('NORMAL','MEDIUM','HIGH','CRITICAL')), "
            + "status TEXT NOT NULL DEFAULT 'SUBMITTED', "
            + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), "
            + "updated_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), resolved_at TEXT)",

        "CREATE TABLE IF NOT EXISTS staff_assignments ("
            + "assignment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "complaint_id INTEGER NOT NULL REFERENCES complaints(complaint_id), "
            + "staff_id INTEGER NOT NULL REFERENCES users(user_id), "
            + "assigned_by INTEGER REFERENCES users(user_id), "
            + "assigned_at TEXT NOT NULL DEFAULT (datetime('now','localtime')), "
            + "status TEXT NOT NULL DEFAULT 'ACTIVE')",

        "CREATE TABLE IF NOT EXISTS complaint_history ("
            + "history_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "complaint_id INTEGER NOT NULL REFERENCES complaints(complaint_id), "
            + "updated_by INTEGER REFERENCES users(user_id), old_status TEXT, new_status TEXT NOT NULL, "
            + "remarks TEXT, updated_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))",

        "CREATE TABLE IF NOT EXISTS attachments ("
            + "attachment_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "complaint_id INTEGER NOT NULL REFERENCES complaints(complaint_id), "
            + "file_name TEXT NOT NULL, file_path TEXT NOT NULL, "
            + "uploaded_by INTEGER REFERENCES users(user_id), "
            + "uploaded_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))",

        "CREATE TABLE IF NOT EXISTS notifications ("
            + "notification_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "user_id INTEGER NOT NULL REFERENCES users(user_id), "
            + "complaint_id INTEGER REFERENCES complaints(complaint_id), message TEXT NOT NULL, "
            + "is_read INTEGER NOT NULL DEFAULT 0, "
            + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))",

        "CREATE TABLE IF NOT EXISTS feedback ("
            + "feedback_id INTEGER PRIMARY KEY AUTOINCREMENT, "
            + "complaint_id INTEGER NOT NULL UNIQUE REFERENCES complaints(complaint_id), "
            + "user_id INTEGER NOT NULL REFERENCES users(user_id), "
            + "rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5), comments TEXT, "
            + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))",

        "CREATE INDEX IF NOT EXISTS idx_complaints_user ON complaints(user_id)",
        "CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status)",
        "CREATE INDEX IF NOT EXISTS idx_history_complaint ON complaint_history(complaint_id)",
        "CREATE INDEX IF NOT EXISTS idx_notif_user ON notifications(user_id)"
    };

    private static final String[][] DEPARTMENTS = {
        {"IT", "Wi-Fi, computers and network"},
        {"Electrical", "Electricity, lights and fans"},
        {"Maintenance", "Furniture, classroom equipment and general maintenance"},
        {"Library", "Library related issues"},
        {"Hostel", "Hostel related issues"},
        {"Transport", "College buses and transport"},
        {"Security", "Campus security"}
    };

    /** {category, department} */
    private static final String[][] CATEGORIES = {
        {"Wi-Fi", "IT"}, {"Computer", "IT"}, {"Network", "IT"},
        {"Electricity", "Electrical"}, {"Lights", "Electrical"}, {"Fans", "Electrical"},
        {"Furniture", "Maintenance"}, {"Maintenance", "Maintenance"},
        {"Classroom Equipment", "Maintenance"}, {"Other", "Maintenance"},
        {"Library", "Library"}, {"Hostel", "Hostel"}, {"Transport", "Transport"}, {"Security", "Security"}
    };

    /** {location, building, floor} */
    private static final String[][] LOCATIONS = {
        {"Main Gate", "Campus", "Ground"}, {"Admin Block", "Admin", "Ground"},
        {"Classroom Block A", "Block A", "1"}, {"Classroom Block B", "Block B", "1"},
        {"Computer Lab 1", "Block A", "2"}, {"Library", "Library", "Ground"},
        {"Boys Hostel", "Hostel", "Ground"}, {"Girls Hostel", "Hostel", "Ground"},
        {"Canteen", "Campus", "Ground"}, {"BUS NO 1", "Transport", "-"},
        {"BUS NO 2", "Transport", "-"}, {"BUS NO 3", "Transport", "-"}, {"Playground", "Campus", "Ground"}
    };

    public static void initialize() {
        AppConfig.ensureDirectories();
        try (Connection c = DatabaseConnection.get()) {
            c.setAutoCommit(false);
            try (Statement st = c.createStatement()) {
                for (String sql : TABLES) st.execute(sql);
            }
            if (isEmpty(c, "departments")) seedMasterData(c);
            if (!userExists(c, AppConfig.DEFAULT_ADMIN_EMAIL)) seedUsers(c);
            c.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static boolean isEmpty(Connection c, String table) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            return rs.next() && rs.getInt(1) == 0;
        }
    }

    private static boolean userExists(Connection c, String email) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM users WHERE email = ?")) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private static void seedMasterData(Connection c) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO departments (department_name, description) VALUES (?, ?)")) {
            for (String[] d : DEPARTMENTS) {
                ps.setString(1, d[0]);
                ps.setString(2, d[1]);
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO categories (category_name, department_id) "
                + "VALUES (?, (SELECT department_id FROM departments WHERE department_name = ?))")) {
            for (String[] cat : CATEGORIES) {
                ps.setString(1, cat[0]);
                ps.setString(2, cat[1]);
                ps.executeUpdate();
            }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO locations (location_name, building, floor) VALUES (?, ?, ?)")) {
            for (String[] l : LOCATIONS) {
                ps.setString(1, l[0]);
                ps.setString(2, l[1]);
                ps.setString(3, l[2]);
                ps.executeUpdate();
            }
        }
    }

    /** Default admin + one demo staff member per department (password hashed with BCrypt). */
    private static void seedUsers(Connection c) throws SQLException {
        String sql = "INSERT INTO users (name, email, password, phone, role, department_id) "
                + "VALUES (?, ?, ?, ?, ?, (SELECT department_id FROM departments WHERE department_name = ?))";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "System Admin");
            ps.setString(2, AppConfig.DEFAULT_ADMIN_EMAIL);
            ps.setString(3, PasswordUtil.hash(AppConfig.DEFAULT_ADMIN_PASSWORD));
            ps.setString(4, "9000000000");
            ps.setString(5, "ADMIN");
            ps.setString(6, null);
            ps.executeUpdate();

            for (String[] d : DEPARTMENTS) {
                ps.setString(1, d[0] + " Staff");
                ps.setString(2, "staff." + d[0].toLowerCase() + "@campuscare.com");
                ps.setString(3, PasswordUtil.hash(AppConfig.DEFAULT_STAFF_PASSWORD));
                ps.setString(4, "9111111111");
                ps.setString(5, "STAFF");
                ps.setString(6, d[0]);
                ps.executeUpdate();
            }
        }
    }
}
