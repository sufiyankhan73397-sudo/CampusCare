-- CampusCare database schema (PostgreSQL)
-- The app creates these tables automatically on first run (DatabaseInitializer.java).
-- This file is for documentation and for setting up a fresh database manually.
-- Default admin/staff users are created by the app (passwords are BCrypt-hashed at runtime).

CREATE TABLE IF NOT EXISTS departments (
    department_id   SERIAL PRIMARY KEY,
    department_name TEXT NOT NULL UNIQUE,
    description     TEXT,
    status          TEXT NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS users (
    user_id       SERIAL PRIMARY KEY,
    name          TEXT NOT NULL,
    email         TEXT NOT NULL UNIQUE,
    password      TEXT NOT NULL,
    phone         TEXT,
    role          TEXT NOT NULL CHECK (role IN ('STUDENT','STAFF','ADMIN')),
    department_id INTEGER REFERENCES departments(department_id),
    status        TEXT NOT NULL DEFAULT 'ACTIVE',
    created_at    TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS')
);

CREATE TABLE IF NOT EXISTS categories (
    category_id   SERIAL PRIMARY KEY,
    category_name TEXT NOT NULL UNIQUE,
    department_id INTEGER NOT NULL REFERENCES departments(department_id)
);

CREATE TABLE IF NOT EXISTS locations (
    location_id   SERIAL PRIMARY KEY,
    location_name TEXT NOT NULL UNIQUE,
    building      TEXT,
    floor         TEXT,
    description   TEXT
);

CREATE TABLE IF NOT EXISTS complaints (
    complaint_id SERIAL PRIMARY KEY,
    user_id      INTEGER NOT NULL REFERENCES users(user_id),
    category_id  INTEGER NOT NULL REFERENCES categories(category_id),
    location_id  INTEGER NOT NULL REFERENCES locations(location_id),
    title        TEXT NOT NULL,
    description  TEXT NOT NULL,
    priority     TEXT NOT NULL CHECK (priority IN ('NORMAL','MEDIUM','HIGH','CRITICAL')),
    status       TEXT NOT NULL DEFAULT 'SUBMITTED',
    created_at   TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS'),
    updated_at   TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS'),
    resolved_at  TEXT
);
CREATE TABLE IF NOT EXISTS staff_assignments (
    assignment_id SERIAL PRIMARY KEY,
    complaint_id  INTEGER NOT NULL REFERENCES complaints(complaint_id),
    staff_id      INTEGER NOT NULL REFERENCES users(user_id),
    assigned_by   INTEGER REFERENCES users(user_id),
    assigned_at   TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS'),
    status        TEXT NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS complaint_history (
    history_id   SERIAL PRIMARY KEY,
    complaint_id INTEGER NOT NULL REFERENCES complaints(complaint_id),
    updated_by   INTEGER REFERENCES users(user_id),
    old_status   TEXT,
    new_status   TEXT NOT NULL,
    remarks      TEXT,
    updated_at   TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS')
);

CREATE TABLE IF NOT EXISTS attachments (
    attachment_id SERIAL PRIMARY KEY,
    complaint_id  INTEGER NOT NULL REFERENCES complaints(complaint_id),
    file_name     TEXT NOT NULL,
    file_path     TEXT NOT NULL,
    uploaded_by   INTEGER REFERENCES users(user_id),
    uploaded_at   TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS')
);

CREATE TABLE IF NOT EXISTS notifications (
    notification_id SERIAL PRIMARY KEY,
    user_id         INTEGER NOT NULL REFERENCES users(user_id),
    complaint_id    INTEGER REFERENCES complaints(complaint_id),
    message         TEXT NOT NULL,
    is_read         INTEGER NOT NULL DEFAULT 0,
    created_at      TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS')
);

CREATE TABLE IF NOT EXISTS feedback (
    feedback_id  SERIAL PRIMARY KEY,
    complaint_id INTEGER NOT NULL UNIQUE REFERENCES complaints(complaint_id),
    user_id      INTEGER NOT NULL REFERENCES users(user_id),
    rating       INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments     TEXT,
    created_at   TEXT NOT NULL DEFAULT to_char(now() AT TIME ZONE 'Asia/Kolkata', 'YYYY-MM-DD HH24:MI:SS')
);

CREATE INDEX IF NOT EXISTS idx_complaints_user   ON complaints(user_id);
CREATE INDEX IF NOT EXISTS idx_complaints_status ON complaints(status);
CREATE INDEX IF NOT EXISTS idx_history_complaint ON complaint_history(complaint_id);
CREATE INDEX IF NOT EXISTS idx_notif_user        ON notifications(user_id);
-- Default master data (only inserted if the tables are empty)
INSERT INTO departments (department_name, description)
SELECT v.n, v.d FROM (VALUES
    ('IT', 'Wi-Fi, computers and network'),
    ('Electrical', 'Electricity, lights and fans'),
    ('Maintenance', 'Furniture, classroom equipment and general maintenance'),
    ('Library', 'Library related issues'),
    ('Hostel', 'Hostel related issues'),
    ('Transport', 'College buses and transport'),
    ('Security', 'Campus security')
) AS v(n, d)
WHERE NOT EXISTS (SELECT 1 FROM departments);

INSERT INTO categories (category_name, department_id)
SELECT v.c, (SELECT department_id FROM departments WHERE department_name = v.d) FROM (VALUES
    ('Wi-Fi', 'IT'), ('Computer', 'IT'), ('Network', 'IT'),
    ('Electricity', 'Electrical'), ('Lights', 'Electrical'), ('Fans', 'Electrical'),
    ('Furniture', 'Maintenance'), ('Maintenance', 'Maintenance'),
    ('Classroom Equipment', 'Maintenance'), ('Other', 'Maintenance'),
    ('Library', 'Library'), ('Hostel', 'Hostel'), ('Transport', 'Transport'), ('Security', 'Security')
) AS v(c, d)
WHERE NOT EXISTS (SELECT 1 FROM categories);

INSERT INTO locations (location_name, building, floor)
SELECT v.n, v.b, v.f FROM (VALUES
    ('Main Gate', 'Campus', 'Ground'), ('Admin Block', 'Admin', 'Ground'),
    ('Classroom Block A', 'Block A', '1'), ('Classroom Block B', 'Block B', '1'),
    ('Computer Lab 1', 'Block A', '2'), ('Library', 'Library', 'Ground'),
    ('Boys Hostel', 'Hostel', 'Ground'), ('Girls Hostel', 'Hostel', 'Ground'),
    ('Canteen', 'Campus', 'Ground'), ('BUS NO 1', 'Transport', '-'),
    ('BUS NO 2', 'Transport', '-'), ('BUS NO 3', 'Transport', '-'), ('Playground', 'Campus', 'Ground')
) AS v(n, b, f)
WHERE NOT EXISTS (SELECT 1 FROM locations);