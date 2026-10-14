# Core / Infrastructure

**Owner:** Sufiyan Khan (Team Lead)

## Purpose
The shared base that every module depends on. Only the Team Lead edits these files.

## Contents
- DatabaseConnection: opens PostgreSQL (Neon) connections and runs a transaction helper (commit on success, rollback on any failure)
- DatabaseInitializer: creates all 10 tables and the default data on first run (7 departments, 14 categories, 13 locations, default admin, demo staff)
- AppConfig: constants, upload rules, database settings read from config.properties (not committed to GitHub)
- Navigator: screen navigation
- pom.xml and packaging: Maven build, shaded jar, jpackage Windows application image
- database.sql: schema and master data for documentation

## Shared files rule
Only the Team Lead edits pom.xml, DatabaseInitializer and main.css. Other members ask the Team Lead for changes.

## Database credentials
The database password is never committed. Each developer keeps their own config.properties (see config.properties.example).