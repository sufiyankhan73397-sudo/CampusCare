# M3 - Complaint Tracking & History

**Owner:** Sufiyan Nagori
**Branch:** [to be filled by owner]

## Purpose
Lets a student follow a complaint from submission to closure, see its complete history and reopen it if the problem is not fixed.

## Features
- My Complaints list with search and filters (text, status, priority, category)
- Complaint details with attachments and status history timeline
- Dashboard counts: pending (SUBMITTED, ASSIGNED, ACCEPTED, REOPENED) and completed (RESOLVED, CLOSED)
- Reopen a RESOLVED complaint with a reason

## Reopen rules
- Only the student who filed the complaint can reopen it
- Only a RESOLVED complaint can be reopened
- Reason: 5 to 500 characters
- Status becomes REOPENED, resolved_at is cleared, the student and the admins are notified
- The complaint is re-assigned automatically to the staff member who worked on it last (or the least-loaded staff member); status becomes ASSIGNED again

## Code
| Layer | Classes |
|---|---|
| Controller | MyComplaintsController, ComplaintDetailsController, HomeController, StudentDashboardController |
| Service | ComplaintService: forStudent, getById, history, attachments, reopen |
| DAO | ComplaintDao, HistoryDao, AttachmentDao |
| Util | ComplaintTable (search and filter) |

## Database tables
complaints, complaint_history, attachments

## Screenshots
See docs/screenshots (my complaints, complaint details).

## Testing
[to be filled: test cases and results]

## Contribution
[to be filled by owner: what was built]