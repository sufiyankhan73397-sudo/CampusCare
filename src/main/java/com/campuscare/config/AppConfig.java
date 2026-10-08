package com.campuscare.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Central place for application constants and folder locations.
 * Data is stored in the user's home folder (C:\Users\<name>\CampusCare) so that
 * the installed EXE can always write to it (Program Files is read-only).
 */
public final class AppConfig {
    public static final String APP_NAME = "CampusCare";
    public static final String APP_TITLE = "CampusCare - Smart Campus Issue Management System";

    public static final Path APP_DIR = Paths.get(System.getProperty("user.home"), "CampusCare");
    public static final Path DATA_DIR = APP_DIR.resolve("data");
    public static final Path DB_FILE = DATA_DIR.resolve("campuscare.db");
    public static final Path UPLOAD_DIR = APP_DIR.resolve("uploads");

    public static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024; // 5 MB
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "pdf");

    public static final String DEFAULT_ADMIN_EMAIL = "admin@campuscare.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    public static final String DEFAULT_STAFF_PASSWORD = "Staff@123";

    private AppConfig() { }

    public static void ensureDirectories() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create application folders: " + e.getMessage(), e);
        }
    }
}
