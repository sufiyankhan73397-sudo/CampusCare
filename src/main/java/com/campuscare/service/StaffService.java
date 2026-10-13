package com.campuscare.service;

import com.campuscare.dao.AssignmentDao;
import com.campuscare.dao.ComplaintDao;
import com.campuscare.dao.UserDao;
import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Complaint;
import com.campuscare.model.Status;
import com.campuscare.util.AppException;
import com.campuscare.util.ValidationUtil;

import java.util.List;
import java.util.Set;

/** Module M4: staff accepts, starts work and resolves complaints assigned to them. */
public class StaffService {
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final UserDao userDao = new UserDao();
    private final NotificationService notifier = new NotificationService();
    private final WorkflowHelper workflow = new WorkflowHelper();

    public List<Complaint> assignedTo(int staffId) { return complaintDao.findByStaff(staffId); }

    public void accept(int complaintId, int staffId, String remarks) {
        move(complaintId, staffId, Set.of(Status.ASSIGNED), Status.ACCEPTED,
                defaultText(remarks, "Staff accepted the complaint"), "accepted");
    }

    public void startWork(int complaintId, int staffId, String remarks) {
        move(complaintId, staffId, Set.of(Status.ASSIGNED, Status.ACCEPTED), Status.IN_PROGRESS,
                defaultText(remarks, "Work started"), "started working on");
    }

    public void resolve(int complaintId, int staffId, String remarks) {
        if (ValidationUtil.isBlank(remarks)) throw new AppException("Remarks are required to resolve a complaint.");
        ValidationUtil.checkLength("Remarks", remarks, 5, 500);
        move(complaintId, staffId, Set.of(Status.IN_PROGRESS), Status.RESOLVED, remarks.trim(), "resolved");
    }

    private String defaultText(String remarks, String fallback) {
        return ValidationUtil.isBlank(remarks) ? fallback : remarks.trim();
    }

    private void move(int complaintId, int staffId, Set<String> allowedFrom, String newStatus,
                      String remarks, String verb) {
        DatabaseConnection.tx(c -> {
            Complaint cp = complaintDao.findById(c, complaintId).orElseThrow(() -> new AppException("Complaint not found."));
            if (!assignmentDao.findActiveStaffId(c, complaintId).map(id -> id == staffId).orElse(false))
                throw new AppException("This complaint is not assigned to you.");
            if (!allowedFrom.contains(cp.status()))
                throw new AppException("Not allowed while the status is " + Status.pretty(cp.status()) + ".");

            workflow.changeStatus(c, cp, newStatus, staffId, remarks);
            String staffName = userDao.findNameById(c, staffId);
            notifier.notify(c, cp.userId(), complaintId,
                    "Complaint #" + complaintId + " (" + cp.title() + ") has been " + verb
                    + " by " + staffName + ". Status: " + Status.pretty(newStatus));
            return null;
        });
    }
}
