# M6 - Analytics, Feedback & Reports

**Owner:** Sumit Yadav
**Branch:** `feature/admin-dashboard`

## Purpose
Helps the administration understand how well complaints are handled, and lets students rate the result.

## Features
- Admin analytics: complaints by status, priority, department and category; total users and total staff
- Student feedback: rating 1 to 5 with an optional comment
- Feedback list, department rating averages and overall rating for the admin
- CSV export of all complaints

## Feedback rules
- Only the student who filed the complaint can give feedback
- Only when the complaint is RESOLVED, and only once per complaint
- Comment: up to 500 characters
- Submitting feedback automatically changes the status to CLOSED and notifies the student and the assigned staff member

## Code
| Layer | Classes |
|---|---|
| Controller | AnalyticsController, FeedbackController |
| Service | AdminService (byStatus, byPriority, byDepartment, byCategory, totalUsers, totalStaff, feedbackRows, departmentRatings, overallRating, exportComplaintsCsv); ComplaintService.giveFeedback |
| DAO | AnalyticsDao, FeedbackDao |
| Util | CsvUtil |

## Database tables
feedback, complaints, users, departments, categories

## Screenshots
See docs/screenshots (analytics, feedback).

## Testing
[to be filled: test cases and results]

## Contribution
[to be filled by owner: what was built]