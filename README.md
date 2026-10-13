# CampusCare - Smart Campus Issue Management and Resolution System

A Windows desktop application that lets students report campus issues (Wi-Fi, electricity, labs, library, hostel, transport and more), routes each complaint to the right department, and lets staff and admins track and resolve it with full history, notifications, feedback and analytics.

> Final-Year B.Tech Project, AI&DS-C, A.Y. 2026-27 (Portal ID: PBL2627-AI&DS-C-021)

---

## Features

- **Authentication & Security** - register and login with role-based access (Student, Staff, Admin); passwords hashed with BCrypt
- **Complaint Reporting** - submit a complaint with category, location, priority, description and image/PDF attachments
- **Complaint Tracking & History** - track status in real time with a full status history and remarks
- **Staff Resolution** - assigned staff accept, start and resolve complaints
- **Notifications** - users are notified on every status change
- **Analytics, Feedback & Reports** - admin dashboard with charts, ratings and CSV export
- **Admin Management** - manage users, departments, categories and locations

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| UI | JavaFX, FXML, JavaFX CSS |
| Database | PostgreSQL (Neon cloud) via JDBC |
| Build | Maven |
| Packaging | jpackage (Windows EXE) |
| IDE / OS | VS Code, Windows 11 |

## Team

| Name | Role |
|---|---|
| Sufiyan Khan | Team Lead |
| Sufiyan Nagori | Member |
| Roshan Jat | Member |
| Sumit Yadav | Member |
| Divyanshu Geel | Member |

**Guide:** Er. Ram Babu Buri
## How to Run

### Prerequisites

- JDK 21
- Maven 3.9+
- Internet connection (the database is hosted on the cloud)

### Steps

1. Clone the repository and switch to the develop branch:

        git clone https://github.com/sufiyankhan73397-sudo/CampusCare.git
        cd CampusCare
        git checkout develop

2. Copy `config.properties.example` to `config.properties` and fill in the database details. The real password is shared privately by the Team Lead. Never commit this file.

3. Run the app:

        mvn clean javafx:run

4. Default admin login: `admin@campuscare.com` / `Admin@123`

### Build the EXE

        mvn clean package -DskipTests
        jpackage --type app-image --name CampusCare --input installer\input --main-jar campuscare.jar --main-class com.campuscare.Launcher --dest installer\output
## Folder Structure

    CampusCare/
    |-- pom.xml
    |-- README.md
    |-- database.sql
    |-- config.properties.example
    |-- data/
    |-- docs/              SRS, UML, ER, mock-ups, test cases, report
    |   |-- modules/
    |   |-- screenshots/
    |-- installer/
    |-- src/main/
        |-- java/com/campuscare/
        |   |-- config, controller, dao, database
        |   |-- model, service, util
        |-- resources/     fxml, css, images

## Screenshots

Screenshots are available in the [docs/screenshots](docs/screenshots) folder.

## Git Workflow

- `main` - final stable code only
- `develop` - integrated code
- `feature/*` - one branch per module owner
- Work on your own branch, then open a Pull Request into `develop`
- Commit message example: `Student: add Report Issue screen`

## Future Scope

- Mobile app (Android) and web portal
- Email / SMS notifications
- Automatic complaint prioritisation using AI
- Role-based approval workflow and SLA tracking