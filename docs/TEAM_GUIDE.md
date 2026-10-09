# CampusCare - Team Push Guide (ek din ka plan)

Har member **apne hi GitHub account** se, **apne module ka code**, **3 alag commits** mein push karega.
Kisi ka password / token share nahi hoga. Commit ki date asli rahegi.

Short paths (neeche har jagah yahi use hua hai):
- `J` = `src/main/java/com/campuscare`
- `R` = `src/main/resources`

Tools: Git for Windows, JDK 21, Maven, VS Code. Terminal = VS Code terminal (PowerShell).

---------------------------------------------------------------------

## DIN KA ORDER

| Step | Kaun | Kya |
|------|------|-----|
| 1 | Khan | Repo banaye, `main` + `develop` push kare, collaborators add kare |
| 2 | Khan | Core, DAO, services, Login/Register wala code `develop` par push kare |
| 3 | Nagori | Apna module push + Pull Request. **Khan sabse pehle isko merge karega** |
| 4 | Roshan, Sumit, Divyanshu | Apna module push + Pull Request (Step 3 ke saath parallel ho sakta hai) |
| 5 | Khan | Teeno PR merge kare (Nagori ka pehle) |
| 6 | Sab | `git checkout develop`, `git pull`, `mvn clean javafx:run` se test |

Dependency: Roshan aur Divyanshu ka code Nagori ke `ComplaintTable` aur `ComplaintDetailsController` use karta hai.
Isliye build tab tak fail dikhega jab tak Nagori ka PR merge nahi hota. Ye normal hai.

---------------------------------------------------------------------

## SAB MEMBERS KE LIYE COMMON STEPS (Khan ke Step 2 ke baad)

```
git clone https://github.com/KHAN-USERNAME/CampusCare.git
cd CampusCare
git config user.name "Apna Naam"
git config user.email "apni-github-email"
git checkout -b feature/XXXX origin/develop
```
(`feature/XXXX` ka naam apne section mein likha hai.)

Phir zip extract karo. Apni files ko **same folder path par** copy-paste karo (File Explorer se),
clone wale `CampusCare` folder mein. Phir neeche likhe 3 commits karo, aakhir mein:

```
git push -u origin feature/XXXX
```
GitHub kholo -> **Compare & pull request** -> base: `develop` -> Create.

---------------------------------------------------------------------

## 1) SUFIYAN KHAN - Team Lead: Core + M1 (Authentication & Security)

### Pehle: repo banao
1. GitHub par naya repo `CampusCare` banao (README / .gitignore / license tick mat karna).
2. Settings -> Collaborators -> baaki 4 ko add karo.
3. Terminal:
```
git clone https://github.com/KHAN-USERNAME/CampusCare.git
cd CampusCare
git config user.name "Sufiyan Khan"
git config user.email "khan-github-email"
```
4. Zip se ye copy karo: `README.md`, `.gitignore`, `docs/` folder. Phir:
```
git add README.md .gitignore docs
git commit -m "Initial commit: README and docs structure"
git branch -M main
git push -u origin main
git checkout -b develop
```
5. GitHub -> Settings -> Branches -> Add rule -> `main` -> "Require a pull request before merging" ON.

### Commits (develop branch par)
Zip se in files ko same path par copy karke har commit ke baad `git add`/`commit` karo.

**Commit 1 - project setup + database**
Copy: `pom.xml`, `run.bat`, `build-exe.bat`, `build-installer.bat`, `J/config/`, `J/database/`
```
git add pom.xml run.bat build-exe.bat build-installer.bat src/main/java/com/campuscare/config src/main/java/com/campuscare/database
git commit -m "Phase 1-6: Maven JavaFX setup, SQLite config, DatabaseInitializer"
```

**Commit 2 - models + DAO layer**
Copy: `J/model/` (poora), `J/dao/` (poora, saari 11 files)
```
git add src/main/java/com/campuscare/model src/main/java/com/campuscare/dao
git commit -m "Phase 7-8: models and DAO layer"
```

**Commit 3 - services + utils**
Copy: `J/service/` (poora), `J/util/` ki ye files: AlertUtil, AppException, CsvUtil, PasswordUtil, SessionManager, UiUtil, ValidationUtil
(**ComplaintTable.java mat lena - wo Nagori ki hai**)
```
git add src/main/java/com/campuscare/service src/main/java/com/campuscare/util
git commit -m "Phase 9: service layer, BCrypt password util, validation, session"
```

**Commit 4 - UI shell + login (M1)**
Copy: `J/MainApp.java`, `J/Launcher.java`, `J/controller/` ki ye files: Navigator, BaseDashboardController, StudentDashboardController, StaffDashboardController, AdminDashboardController, LoginController, RegisterController, ProfileController
`R/fxml/`: Login, Register, Dashboard, Profile; `R/css/` (4 files); `R/images/.gitkeep`
```
git add src/main/java/com/campuscare/MainApp.java src/main/java/com/campuscare/Launcher.java src/main/java/com/campuscare/controller src/main/resources/css src/main/resources/images src/main/resources/fxml/Login.fxml src/main/resources/fxml/Register.fxml src/main/resources/fxml/Dashboard.fxml src/main/resources/fxml/Profile.fxml
git commit -m "Phase 10-12: authentication, role-based dashboards, login and registration UI"
git push -u origin develop
```

### Baad mein
- Teeno PR review karke merge karo (**pehle Nagori**).
- Merge ke baad: `git checkout develop`, `git pull`, `mvn clean javafx:run`.
- Koi compile error aaye to mujhe error text bhejo.

### Viva ke liye samjho (M1 + Core)
- Layers: FXML -> Controller -> Service -> DAO -> JDBC -> SQLite. Har layer ka kaam.
- `PasswordUtil`: BCrypt hash, salt automatically, plain password kabhi DB mein nahi.
- `AuthService.login`: email se user, hash verify, inactive user block, `SessionManager.login`.
- `Navigator.showDashboard`: role ke hisaab se Student/Staff/Admin dashboard (role-based access).
- `DatabaseInitializer`: `IF NOT EXISTS`, default departments / categories / admin sirf ek baar seed hote hain.
- `DatabaseConnection.tx`: transaction, error par rollback. Isliye status + history + notification saath save hote hain.
- Hamesha `PreparedStatement` -> SQL injection nahi.
- DB `C:\Users\<name>\CampusCare\data` mein kyun? Installed EXE `Program Files` mein likh nahi sakta.
- `Launcher` class alag kyun? Fat JAR mein JavaFX "runtime components missing" error se bachne ke liye.
- Q: duplicate email kaise rokta hai? `UNIQUE` constraint + `emailExists` check.

---------------------------------------------------------------------

## 2) SUFIYAN NAGORI - M2 Complaint Reporting + M3 Tracking & History
Branch: `feature/student`

### Files copy karo
- `J/util/ComplaintTable.java`
- `J/controller/`: HomeController, SubmitComplaintController, MyComplaintsController, ComplaintDetailsController
- `R/fxml/`: Home, SubmitComplaint, MyComplaints, ComplaintDetails

### 3 commits
```
git add src/main/resources/fxml/Home.fxml src/main/resources/fxml/SubmitComplaint.fxml src/main/resources/fxml/MyComplaints.fxml src/main/resources/fxml/ComplaintDetails.fxml
git commit -m "Student UI: dashboard, report issue, my complaints, details FXML"

git add src/main/java/com/campuscare/util/ComplaintTable.java src/main/java/com/campuscare/controller/HomeController.java src/main/java/com/campuscare/controller/SubmitComplaintController.java
git commit -m "Phase 13-14: student dashboard stats, complaint submission, reusable complaint table"

git add src/main/java/com/campuscare/controller/MyComplaintsController.java src/main/java/com/campuscare/controller/ComplaintDetailsController.java
git commit -m "Phase 15-17, 29-30: my complaints, details, dynamic timeline, reopen, feedback, search/filter"
git push -u origin feature/student
```

### Test (Khan ke merge ke baad)
1. Register -> login -> Report Issue: "Bus AC not working", Transport, BUS NO 3, HIGH.
2. Department "Transport (automatic)" dikhna chahiye; submit par Assigned staff dikhe.
3. My Complaints -> Details: timeline mein SUBMITTED aur ASSIGNED dikhe.
4. Filters (status, priority, category) aur search test karo.

### Viva ke liye samjho
- Student department nahi chunta: `category.department_id` se. SQL join `ComplaintDao` ke `BASE` query mein.
- `ComplaintService.submit`: validation -> transaction: complaint insert, history (`old_status = NULL`, "Complaint submitted"), notification, attachment, auto-assign.
- Auto-assign: `AssignmentDao.findLeastLoadedStaff` (sabse kam open complaints wala staff).
- Attachment safety: sirf png/jpg/jpeg/pdf, max 5 MB, file ka naam UUID (path attack nahi).
- Timeline hardcoded nahi: `HistoryDao.findByComplaint` -> `ComplaintDetailsController.timelineRow`.
- Reopen: sirf apni + sirf RESOLVED complaint; pichle staff ko wapas assign.
- `ComplaintTable`: `FilteredList` (filter) + `SortedList` (column sort).
- Details mein ID nahi, naam dikhte hain (JOIN).

---------------------------------------------------------------------

## 3) ROSHAN JAT - M4 Staff Resolution + M5 Notification
Branch: `feature/staff`

### Files copy karo
- `J/controller/`: StaffComplaintController, NotificationsController
- `R/fxml/`: StaffComplaint, Notifications

### 3 commits
```
git add src/main/resources/fxml/StaffComplaint.fxml src/main/resources/fxml/Notifications.fxml
git commit -m "Staff UI: assigned complaints screen and notifications screen FXML"

git add src/main/java/com/campuscare/controller/NotificationsController.java
git commit -m "Phase 18: notifications list with read/unread and mark all read"

git add src/main/java/com/campuscare/controller/StaffComplaintController.java
git commit -m "Phase 19-21: staff assigned complaints, accept, start work, resolve with remarks"
git push -u origin feature/staff
```

### Test
Staff login `staff.transport@campuscare.com` / `Staff@123`:
1. Assigned Complaints -> complaint chuno -> **Accept**.
2. **Start Work** -> status IN PROGRESS.
3. Remarks likho -> **Mark Resolved** (remarks bina error aata hai).
4. Student login -> Notifications mein sab alerts dikhe.

### Viva ke liye samjho
- Status flow: ASSIGNED -> ACCEPTED -> IN_PROGRESS -> RESOLVED (`StaffService`).
- `StaffService.move`: (1) complaint isi staff ko assigned hai? (2) abhi ka status allowed hai? Warna error.
- Har status change `WorkflowHelper.changeStatus` se: `complaints.status` + `updated_at` update, `complaint_history` row.
- Notification usi transaction mein banti hai (`NotificationService.notify`).
- Buttons enable/disable: `StaffComplaintController.updateButtons`.
- Resolve par remarks zaroori kyun? Student timeline mein dikhta hai (accountability).
- Notifications: `is_read` 0/1, sidebar mein unread count (`BaseDashboardController.refreshBadge`).
- Q: staff kisi aur ka complaint resolve kare to? Block hota hai (assignment check).

---------------------------------------------------------------------

## 4) SUMIT YADAV - M6 Analytics, Feedback & Reports
Branch: `feature/admin-dashboard`

### Files copy karo
- `J/controller/`: AnalyticsController, FeedbackController
- `R/fxml/`: Analytics, Feedback

### 3 commits
```
git add src/main/resources/fxml/Analytics.fxml src/main/resources/fxml/Feedback.fxml
git commit -m "Admin UI: analytics dashboard cards and charts, feedback screen FXML"

git add src/main/java/com/campuscare/controller/AnalyticsController.java
git commit -m "Phase 22, 27: admin dashboard counts and charts (status pie, department, priority, category)"

git add src/main/java/com/campuscare/controller/FeedbackController.java
git commit -m "Phase 28: feedback view with average rating, total and department-wise ratings"
git push -u origin feature/admin-dashboard
```

### Test
1. Admin login `admin@campuscare.com` / `Admin@123` -> Overview: cards + 4 charts.
2. Student se kuch complaints banao / resolve karwao, feedback do -> numbers update ho.
3. Feedback screen: average rating, total, department table.

### Viva ke liye samjho
- Charts ka data `AnalyticsDao` ki `GROUP BY` queries se (status, priority, department, category).
- PieChart vs BarChart: kab kya. `XYChart.Series` / `XYChart.Data`.
- Feedback: rating 1-5 (DB `CHECK`), ek complaint par ek hi feedback (`UNIQUE`).
- Feedback dene par complaint **CLOSED** (`ComplaintService.giveFeedback`).
- Department-wise average: `FeedbackDao.averageByDepartment` (AVG + GROUP BY).
- Reports: `CsvUtil.exportComplaints` (Excel mein khulta hai, quotes escape hote hain). Button admin complaints screen mein hai.
- Open complaints = total - (RESOLVED + CLOSED).

---------------------------------------------------------------------

## 5) DIVYANSHU GEEL - M7 Admin Management
Branch: `feature/admin-mgmt`

### Files copy karo
- `J/controller/`: UserManagementController, DepartmentController, CategoryController, LocationController, AdminComplaintsController
- `R/fxml/`: UserManagement, Department, Category, Location, AdminComplaints

### 3 commits
```
git add src/main/resources/fxml/UserManagement.fxml src/main/resources/fxml/Department.fxml src/main/resources/fxml/Category.fxml src/main/resources/fxml/Location.fxml src/main/resources/fxml/AdminComplaints.fxml
git commit -m "Admin management UI: users, departments, categories, locations, complaints FXML"

git add src/main/java/com/campuscare/controller/DepartmentController.java src/main/java/com/campuscare/controller/CategoryController.java src/main/java/com/campuscare/controller/LocationController.java
git commit -m "Phase 24-26: department, category and location CRUD"

git add src/main/java/com/campuscare/controller/UserManagementController.java src/main/java/com/campuscare/controller/AdminComplaintsController.java
git commit -m "Phase 23: user management, complaint assignment, CSV export"
git push -u origin feature/admin-mgmt
```

### Test
1. Users -> Add User: STAFF + department; invalid email / weak password par error. Deactivate / Activate.
2. Departments: add, update, deactivate. Categories: add (department mapping), delete.
3. Locations: add / update / delete.
4. Complaints: complaint chuno -> us department ka staff chuno -> **Assign**. Export CSV.

### Viva ke liye samjho
- Admin-only: `AdminService.requireAdmin` (role check service level par bhi).
- Naya user: `AuthService.createUser` -> validation, duplicate email, BCrypt. STAFF ke liye department zaroori.
- Apna account deactivate nahi hota.
- Department delete nahi, **status toggle** (history / FK safe).
- Category delete par agar complaints use kar rahi hain to DB foreign key error -> user-friendly message.
- Assignment: sirf SUBMITTED / ASSIGNED / ACCEPTED / REOPENED status mein; staff usi department ka hona chahiye (`isActiveStaffOfDepartment`).
- `WorkflowHelper.assign`: purane assignments `REASSIGNED`, naya `ACTIVE`, status ASSIGNED, history + dono ko notification.
- Category -> Department mapping hi automatic routing ka base hai.

---------------------------------------------------------------------

## ANT MEIN (Khan ke liye): final check
```
git checkout develop
git pull
mvn clean javafx:run
```
End-to-end test (Bus AC): student submit -> staff accept -> start -> resolve -> student feedback -> CLOSED -> admin analytics.
Sab theek ho to `develop` ko `main` mein PR se merge karo, `mvn clean package` se `target/campuscare.jar` banao.

## Common galtiyan
- Files same path par nahi rakhi -> "package does not exist". Path `src/main/java/com/campuscare/...` hi hona chahiye.
- `develop` pull kiye bina build -> dusre ka code missing.
- `target/`, `*.db`, `uploads/` commit mat karo (`.gitignore` rok deta hai).
- Kisi aur ke naam se commit mat karo. Har member khud push kare.
