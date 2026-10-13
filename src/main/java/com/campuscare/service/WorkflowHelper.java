package com.campuscare.service;

import com.campuscare.dao.AssignmentDao;
import com.campuscare.dao.ComplaintDao;
import com.campuscare.dao.HistoryDao;
import com.campuscare.model.Complaint;
import com.campuscare.model.Status;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * The one place where a complaint's status changes.
 * Every change: (1) updates complaints.status + updated_at, (2) inserts a complaint_history row.
 * Notifications are created by the caller (they differ per action).
 */
class WorkflowHelper {
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final HistoryDao historyDao = new HistoryDao();
    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final NotificationService notifier = new NotificationService();

    void changeStatus(Connection c, Complaint cp, String newStatus, Integer byUserId, String remarks)
            throws SQLException {
        complaintDao.updateStatus(c, cp.id(), newStatus);
        historyDao.insert(c, cp.id(), byUserId, cp.status(), newStatus, remarks);
    }

    /** Assign (or re-assign) to a staff member -> status ASSIGNED, notify student and staff. */
    void assign(Connection c, Complaint cp, int staffId, Integer byUserId, String remarks) throws SQLException {
        assignmentDao.deactivateAll(c, cp.id());
        assignmentDao.insert(c, cp.id(), staffId, byUserId);
        changeStatus(c, cp, Status.ASSIGNED, byUserId, remarks);
        notifier.notify(c, cp.userId(), cp.id(),
                "Complaint #" + cp.id() + " (" + cp.title() + ") has been assigned to " + cp.departmentName() + " staff.");
        notifier.notify(c, staffId, cp.id(),
                "New complaint assigned to you: #" + cp.id() + " " + cp.title() + " [" + cp.priority() + "]");
    }
}
