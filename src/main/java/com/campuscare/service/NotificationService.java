package com.campuscare.service;

import com.campuscare.dao.NotificationDao;
import com.campuscare.model.Notification;
import com.campuscare.util.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** In-app notifications (submitted, assigned, accepted, started, resolved, reopened ...). */
public class NotificationService {
    private final NotificationDao dao = new NotificationDao();

    /** Called inside a transaction together with the status change. */
    public void notify(Connection c, int userId, Integer complaintId, String message) throws SQLException {
        dao.insert(c, userId, complaintId, message);
    }

    public List<Notification> myNotifications() {
        return dao.findByUser(SessionManager.requireUser().id());
    }

    public int unreadCount() {
        return dao.countUnread(SessionManager.requireUser().id());
    }

    public void markRead(int notificationId) {
        dao.markRead(notificationId, SessionManager.requireUser().id());
    }

    public void markAllRead() {
        dao.markAllRead(SessionManager.requireUser().id());
    }
}
