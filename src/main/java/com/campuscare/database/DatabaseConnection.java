package com.campuscare.database;

import com.campuscare.config.AppConfig;
import com.campuscare.util.AppException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens PostgreSQL (Neon) connections and provides a small transaction helper. */
public final class DatabaseConnection {
    private DatabaseConnection() { }

    /** Work executed inside one transaction. */
    @FunctionalInterface
    public interface TxWork<T> {
        T run(Connection c) throws SQLException;
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(AppConfig.dbUrl(), AppConfig.dbUser(), AppConfig.dbPassword());
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

    /** Id of the row inserted last on this connection (works after an insert into a SERIAL table). */
    public static int lastInsertId(Connection c) throws SQLException {
        try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT lastval()")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}