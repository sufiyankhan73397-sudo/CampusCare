package com.campuscare.util;

import java.sql.SQLException;

/** Unchecked exception whose message is safe to show to the user. */
public class AppException extends RuntimeException {
    public AppException(String message) { super(message); }
    public AppException(String message, Throwable cause) { super(message, cause); }

    public static AppException db(SQLException e) {
        return new AppException("Database error: " + e.getMessage(), e);
    }
}
