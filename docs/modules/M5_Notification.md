# M5 - Notification

**Owner:** Roshan Jat
**Branch:** [to be filled by owner]

## Purpose
Keeps every user informed about what happens to their complaints, inside the application.

## Features
- In-app notifications for submission, assignment, accept, start, resolve, reopen and closure
- Unread count
- Mark one notification as read, or mark all as read

## Who is notified
| Event | Notified |
|---|---|
| Complaint submitted | Student, all active admins |
| Complaint assigned | Student, assigned staff member |
| Accept, Start Work, Resolve | Student |
| Reopen | Student, all active admins |
| Feedback and closure | Student, assigned staff member (with the rating) |

## Code
| Layer | Classes |
|---|---|
| Controller | NotificationsController |
| Service | NotificationService: notify, myNotifications, unreadCount, markRead, markAllRead |
| DAO | NotificationDao: insert, findByUser, countUnread, markRead, markAllRead |

## Database tables
notifications

## Design note
Notifications are created inside the same database transaction as the status change, so a failed change never leaves a stray notification.

## Screenshots
See docs/screenshots (notifications).

## Testing
[to be filled: test cases and results]

## Contribution
[to be filled by owner: what was built]