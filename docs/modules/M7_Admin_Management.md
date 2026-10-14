# M7 - Admin Management

**Owner:** Divyanshu Geel
**Branch:** `feature/admin-mgmt`

## Purpose
Lets the administrator control users, master data and complaint assignment.

## Features
- View all complaints
- Assign or re-assign a complaint to an active staff member of its department
- Add users and activate or deactivate them
- Departments: add, update, activate or deactivate
- Categories: add, update, delete
- Locations: add, update, delete

## Rules
- Admin can assign only complaints that are SUBMITTED, ASSIGNED, ACCEPTED or REOPENED
- A new assignment deactivates earlier assignments, sets the status to ASSIGNED and notifies the student and the staff member
- Every category belongs to exactly one department, and a complaint's department comes from its category
- Every admin method checks that the logged-in user is an admin

## Code
| Layer | Classes |
|---|---|
| Controller | AdminDashboardController, AdminComplaintsController, UserManagementController, DepartmentController, CategoryController, LocationController |
| Service | AdminService: users, addUser, setUserStatus, departments, addDepartment, updateDepartment, setDepartmentActive, categories, addCategory, updateCategory, deleteCategory, locations, addLocation, updateLocation, deleteLocation, allComplaints, staffOfDepartment, assign |
| DAO | UserDao, DepartmentDao, CategoryDao, LocationDao, AssignmentDao, ComplaintDao |

## Database tables
users, departments, categories, locations, staff_assignments

## Screenshots
See docs/screenshots (admin dashboard, user management).

## Testing
[to be filled: test cases and results]

## Contribution
[to be filled by owner: what was built]