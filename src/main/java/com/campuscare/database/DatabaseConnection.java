package com.campuscare.database;

import com.campuscare.config.AppConfig;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens SQLite connections and provides a small transaction helper. */
public final class DatabaseConnection {
    private DatabaseConnection() { }

    /** Work executed inside one transaction. */
    @FunctionalInterface
    public interface TxWork<T> {
        T run(Connection c) throws SQLException;
    }

    public static Connection get() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + AppConfig.DB_FILE.toAbsolutePath());
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    /** Runs work in a transaction: commit on success, rollback on any failure. */
    public static <T> T tx(TxWork<T> work) {
        try (Connection c = get()) {
            c.setAutoCommit(false);
            try {
                T result = work.run(c);
                c.commit();
                return result;
            } catch (AppException e) {
                c.rollback();
                throw e;
            } catch (Exception e) {
                c.rollback();
                throw new AppException("Database error: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new AppException("Database error: " + e.getMessage(), e);
        }
    }

    /** Id of the row inserted last on this connection. */
    public static int lastInsertId(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
