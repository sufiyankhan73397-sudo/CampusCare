# M4 - Staff Resolution

**Owner:** Roshan Jat
**Branch:** [to be filled by owner]

## Purpose
Gives each staff member a work list of the complaints assigned to them and lets them move a complaint to resolution.

## Features
- Staff dashboard with the complaints assigned to the logged-in staff member
- Accept, Start Work and Resolve actions
- Every action updates the status, writes a history row and notifies the student

## Rules
| Action | Allowed from | Remarks |
|---|---|---|
| Accept | ASSIGNED | optional |
| Start Work | ASSIGNED or ACCEPTED (accepting first is optional) | optional |
| Resolve | IN_PROGRESS | required, 5 to 500 characters |

- Only the active assignee of the complaint can act on it
- Every status change goes through WorkflowHelper, which updates status and updated_at and stores a complaint_history row

## Code
| Layer | Classes |
|---|---|
| Controller | StaffDashboardController, StaffComplaintController |
| Service | StaffService: assignedTo, accept, startWork, resolve; WorkflowHelper |
| DAO | ComplaintDao, AssignmentDao, UserDao |

## Database tables
complaints, staff_assignments, complaint_history, notifications

## Screenshots
See docs/screenshots (staff dashboard).

## Testing
[to be filled: test cases and results]

## Contribution
[to be filled by owner: what was built]