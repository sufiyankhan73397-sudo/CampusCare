# CampusCare - Smart Campus Issue Management and Resolution System

Windows desktop app (JavaFX + PostgreSQL). B.Tech AI&DS-C, A.Y. 2026-27, Portal ID PBL2627-AI&DS-C-021.
Guide: Er. Ram Babu Buri.

## Tech stack
Java 21, JavaFX 21, Maven, PostgreSQL (JDBC), FXML, JavaFX CSS, BCrypt, jpackage.
## Run (development)
```
mvn clean javafx:run        (or double-click run.bat)
```
Database: PostgreSQL. Configure the database connection using the project's
supported configuration method. Complaint records are stored in PostgreSQL.

## Default logins
| Role  | Email                          | Password  |
|-------|--------------------------------|-----------|
| Admin | admin@campuscare.com           | Admin@123 |
| Staff | staff.transport@campuscare.com | Staff@123 |
| Staff | staff.it / .electrical / .maintenance / .library / .hostel / .security @campuscare.com | Staff@123 |
| Student | register from the Login screen | - |

## Build EXE
- `build-exe.bat`        -> dist\CampusCare\CampusCare.exe (portable, bundled Java, no extra tool)
- `build-installer.bat`  -> dist\CampusCare-1.0.0.exe (setup wizard, needs WiX Toolset 3.x)

## Architecture
```
JavaFX / FXML -> Controller -> Service -> DAO -> JDBC -> PostgreSQL
```
Packages (`com.campuscare`): config, database, model, dao, service, controller, util.

## Complaint lifecycle
SUBMITTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> RESOLVED -> CLOSED
Alternative: RESOLVED -> REOPENED -> ASSIGNED -> IN_PROGRESS -> RESOLVED.
Every change updates complaints.status + updated_at, inserts a complaint_history row (WorkflowHelper)
and creates notifications.

## End-to-end test (Transport / BUS NO 3)
1. Register student -> login -> Report Issue: "Bus AC not working", Transport, BUS NO 3, HIGH.
2. Department shows Transport automatically; complaint auto-assigned to Transport Staff.
3. Logout -> login staff.transport@campuscare.com -> Accept -> Start Work -> Mark Resolved (remarks).
4. Login student -> Notifications -> My Complaints -> Details (timeline) -> Give Feedback -> CLOSED.
5. Login admin -> Overview charts, Feedback, Complaints -> Export CSV.

## Git branches
feature/student, feature/staff, feature/admin-dashboard, feature/admin-mgmt. No direct push to main.
