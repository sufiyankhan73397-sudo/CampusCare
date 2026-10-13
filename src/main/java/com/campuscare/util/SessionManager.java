package com.campuscare.util;

import com.campuscare.model.User;

/** Holds the logged-in user for the lifetime of the session. */
public final class SessionManager {
    private static User current;

    private SessionManager() { }

    public static void login(User user) { current = user; }
    public static void logout() { current = null; }
    public static User getUser() { return current; }
    public static boolean isLoggedIn() { return current != null; }
    public static boolean hasRole(String role) { return current != null && current.role().equals(role); }

    public static User requireUser() {
        if (current == null) throw new AppException("Session expired. Please login again.");
        return current;
    }

    public static void requireRole(String role) {
        if (!hasRole(role)) throw new AppException("You are not authorized to perform this action.");
    }
}
