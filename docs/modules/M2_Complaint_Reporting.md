# M2 - Complaint Reporting

**Owner:** Sufiyan Nagori
**Branch:** `feature/student`

## Purpose
Lets a student report a campus issue in one form. The complaint is stored, the right people are notified and it is assigned automatically.

## Features
- Submit a complaint with title, description, category, location and priority
- Optional attachment (png, jpg, jpeg or pdf, maximum 5 MB)
- History row written as SUBMITTED, student gets a confirmation
- Every active admin is notified about the new complaint
- Auto-assignment to the least-loaded active staff member of the complaint's department; if none is available the complaint stays SUBMITTED for the admin

## Code
| Layer | Classes |
|---|---|
| Controller | SubmitComplaintController |
| Service | ComplaintService: categories, locations, submit |
| DAO | ComplaintDao (insert), AttachmentDao, CategoryDao, LocationDao, AssignmentDao (findLeastLoadedStaff) |
| Config | AppConfig (allowed extensions, 5 MB limit, upload folder) |

## Database tables
complaints, attachments, complaint_history, notifications, staff_assignments

## Validation rules
- Title: 5 to 100 characters
- Description: 10 to 1000 characters
- Category, location and priority are mandatory
- Priority: NORMAL, MEDIUM, HIGH or CRITICAL
- Attachments are saved under a random file name; only the path is stored in the database

## Screenshots
See docs/screenshots (submit complaint).

## Testing
[to be filled: test cases and results]

## Contribution
[to be filled by owner: what was built]