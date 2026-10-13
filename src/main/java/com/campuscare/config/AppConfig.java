package com.campuscare.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;

/**
 * Central place for application constants and folder locations.
 * Uploads are stored in the user's home folder (C:\Users\<name>\CampusCare).
 * Database settings (PostgreSQL / Neon) are read from, in this order:
 *   1. config.properties in the current folder
 *   2. config.properties in C:\Users\<name>\CampusCare
 *   3. app-config.properties bundled inside the jar (used by the packaged EXE)
 * None of these files is committed to GitHub.
 */
public final class AppConfig {
    public static final String APP_NAME = "CampusCare";
    public static final String APP_TITLE = "CampusCare - Smart Campus Issue Management System";

    public static final Path APP_DIR = Paths.get(System.getProperty("user.home"), "CampusCare");
    public static final Path DATA_DIR = APP_DIR.resolve("data");
    /** Old SQLite file path. No longer used for the database; kept so other code still compiles. */
    public static final Path DB_FILE = DATA_DIR.resolve("campuscare.db");
    public static final Path UPLOAD_DIR = APP_DIR.resolve("uploads");

    public static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024; // 5 MB
    public static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "pdf");

    public static final String DEFAULT_ADMIN_EMAIL = "admin@campuscare.com";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";
    public static final String DEFAULT_STAFF_PASSWORD = "Staff@123";

    private static Properties dbProps;

    private AppConfig() { }

    public static void ensureDirectories() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(UPLOAD_DIR);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create application folders: " + e.getMessage(), e);
        }
    }

    public static String dbUrl() { return dbProperty("db.url"); }

    public static String dbUser() { return dbProperty("db.user"); }

    public static String dbPassword() { return dbProperty("db.password"); }

    private static synchronized String dbProperty(String key) {
        if (dbProps == null) {
            dbProps = loadDbProperties();
        }
        String value = dbProps.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing '" + key + "' in database configuration");
        }
        return value.trim();
    }

    private static Properties loadDbProperties() {
        Path[] candidates = { Paths.get("config.properties"), APP_DIR.resolve("config.properties") };
        for (Path p : candidates) {
            if (Files.isRegularFile(p)) {
                try (InputStream in = Files.newInputStream(p)) {
                    Properties props = new Properties();
                    props.load(in);
                    return props;
                } catch (IOException e) {
                    throw new IllegalStateException("Cannot read " + p + ": " + e.getMessage(), e);
                }
            }
        }
        try (InputStream in = AppConfig.class.getResourceAsStream("/app-config.properties")) {
            if (in != null) {
                Properties props = new Properties();
                props.load(in);
                return props;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read bundled app-config.properties: " + e.getMessage(), e);
        }
        throw new IllegalStateException(
                "Database configuration not found. Copy config.properties.example to config.properties "
                + "and fill in the database details.");
    }
}