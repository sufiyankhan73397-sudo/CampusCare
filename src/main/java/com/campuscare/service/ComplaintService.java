package com.campuscare.service;

import com.campuscare.config.AppConfig;
import com.campuscare.dao.AssignmentDao;
import com.campuscare.dao.AttachmentDao;
import com.campuscare.dao.CategoryDao;
import com.campuscare.dao.ComplaintDao;
import com.campuscare.dao.FeedbackDao;
import com.campuscare.dao.HistoryDao;
import com.campuscare.dao.LocationDao;
import com.campuscare.dao.UserDao;
import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Attachment;
import com.campuscare.model.Category;
import com.campuscare.model.Complaint;
import com.campuscare.model.HistoryEntry;
import com.campuscare.model.Location;
import com.campuscare.model.Status;
import com.campuscare.util.AppException;
import com.campuscare.util.ValidationUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Modules M2 + M3: submit, list, details, history, reopen, feedback. */
public class ComplaintService {
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final HistoryDao historyDao = new HistoryDao();
    private final AssignmentDao assignmentDao = new AssignmentDao();
    private final AttachmentDao attachmentDao = new AttachmentDao();
    private final FeedbackDao feedbackDao = new FeedbackDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final LocationDao locationDao = new LocationDao();
    private final UserDao userDao = new UserDao();
    private final NotificationService notifier = new NotificationService();
    private final WorkflowHelper workflow = new WorkflowHelper();

    // ---------- reads ----------
    public List<Category> categories() { return categoryDao.findAll(); }
    public List<Location> locations() { return locationDao.findAll(); }
    public List<Complaint> forStudent(int userId) { return complaintDao.findByUser(userId); }
    public List<Complaint> forStaff(int staffId) { return complaintDao.findByStaff(staffId); }
    public List<HistoryEntry> history(int complaintId) { return historyDao.findByComplaint(complaintId); }
    public List<Attachment> attachments(int complaintId) { return attachmentDao.findByComplaint(complaintId); }

    public Complaint getById(int id) {
        return complaintDao.findById(id).orElseThrow(() -> new AppException("Complaint not found."));
    }

    // ---------- submit ----------
    /**
     * Creates the complaint. The department is NOT chosen by the student: it comes from
     * category.department_id. If that department has an active staff member, the complaint is
     * auto-assigned to the one with the lowest workload; otherwise admin assigns it manually.
     */
    public Complaint submit(int userId, Category category, Location location, String title,
                            String description, String priority, File attachment) {
        if (category == null) throw new AppException("Select a category.");
        if (location == null) throw new AppException("Select a location.");
        ValidationUtil.checkLength("Title", title, 5, 100);
        ValidationUtil.checkLength("Description", description, 10, 1000);
        if (priority == null || !Status.PRIORITIES.contains(priority)) throw new AppException("Select a priority.");
        String ext = attachment == null ? null : validateAttachment(attachment);

        int newId = DatabaseConnection.tx(c -> {
            int cid = complaintDao.insert(c, userId, category.id(), location.id(), title.trim(), description.trim(), priority);
            historyDao.insert(c, cid, userId, null, Status.SUBMITTED, "Complaint submitted");
            notifier.notify(c, userId, cid, "Complaint #" + cid + " (" + title.trim() + ") submitted successfully.");
            if (attachment != null) saveAttachment(c, cid, userId, attachment, ext);

            Complaint cp = complaintDao.findById(c, cid).orElseThrow(() -> new AppException("Complaint not found."));
            for (int adminId : userDao.findActiveIdsByRole(c, Status.ROLE_ADMIN))
                notifier.notify(c, adminId, cid, "New complaint #" + cid + ": " + cp.title() + " [" + priority + "]");

            Optional<Integer> staff = assignmentDao.findLeastLoadedStaff(c, cp.departmentId());
            if (staff.isPresent()) {
                String name = userDao.findNameById(c, staff.get());
                workflow.assign(c, cp, staff.get(), null, "Auto-assigned to " + cp.departmentName() + " staff: " + name);
            }
            return cid;
        });
        return getById(newId);
    }

    private String validateAttachment(File f) {
        String name = f.getName();
        int dot = name.lastIndexOf('.');
        String ext = dot < 0 ? "" : name.substring(dot + 1).toLowerCase();
        if (!AppConfig.ALLOWED_EXTENSIONS.contains(ext)) throw new AppException("Only PNG, JPG and PDF files are allowed.");
        if (f.length() > AppConfig.MAX_UPLOAD_BYTES) throw new AppException("Attachment must be smaller than 5 MB.");
        return ext;
    }

    private void saveAttachment(Connection c, int complaintId, int userId, File f, String ext) throws SQLException {
        Path dest = AppConfig.UPLOAD_DIR.resolve(UUID.randomUUID() + "." + ext); // random name = safe path
        try {
            Files.copy(f.toPath(), dest);
        } catch (IOException e) {
            throw new AppException("Could not save the attachment: " + e.getMessage(), e);
        }
        String safeName = f.getName().replaceAll("[^A-Za-z0-9._ -]", "_");
        attachmentDao.insert(c, complaintId, safeName, dest.toString(), userId);
    }

    // ---------- reopen ----------
    public void reopen(int complaintId, int userId, String reason) {
        if (ValidationUtil.isBlank(reason)) throw new AppException("Please enter a reason for reopening.");
        ValidationUtil.checkLength("Reason", reason, 5, 500);
        DatabaseConnection.tx(c -> {
            Complaint cp = complaintDao.findById(c, complaintId).orElseThrow(() -> new AppException("Complaint not found."));
            if (cp.userId() != userId) throw new AppException("You can reopen only your own complaints.");
            if (!Status.RESOLVED.equals(cp.status())) throw new AppException("Only RESOLVED complaints can be reopened.");

            workflow.changeStatus(c, cp, Status.REOPENED, userId, "Reopened by student: " + reason.trim());
            notifier.notify(c, userId, complaintId, "Complaint #" + complaintId + " has been reopened.");
            for (int adminId : userDao.findActiveIdsByRole(c, Status.ROLE_ADMIN))
                notifier.notify(c, adminId, complaintId, "Complaint #" + complaintId + " was reopened by the student.");

            Complaint reopened = complaintDao.findById(c, complaintId).orElseThrow();
            Optional<Integer> staff = assignmentDao.findLastStaffId(c, complaintId);
            if (staff.isEmpty()) staff = assignmentDao.findLeastLoadedStaff(c, cp.departmentId());
            if (staff.isPresent())
                workflow.assign(c, reopened, staff.get(), null, "Re-assigned after reopen");
            return null;
        });
    }

    // ---------- feedback ----------
    public void giveFeedback(int complaintId, int userId, int rating, String comments) {
        if (rating < 1 || rating > 5) throw new AppException("Rating must be between 1 and 5.");
        if (comments != null && comments.length() > 500) throw new AppException("Comments must be under 500 characters.");
        DatabaseConnection.tx(c -> {
            Complaint cp = complaintDao.findById(c, complaintId).orElseThrow(() -> new AppException("Complaint not found."));
            if (cp.userId() != userId) throw new AppException("You can rate only your own complaints.");
            if (!Status.RESOLVED.equals(cp.status())) throw new AppException("You can give feedback only after the complaint is RESOLVED.");
            if (feedbackDao.existsForComplaint(c, complaintId)) throw new AppException("Feedback was already submitted.");

            feedbackDao.insert(c, complaintId, userId, rating, ValidationUtil.clean(comments));
            workflow.changeStatus(c, cp, Status.CLOSED, userId, "Feedback received (" + rating + "/5). Complaint closed.");
            notifier.notify(c, userId, complaintId, "Thank you! Complaint #" + complaintId + " is now CLOSED.");
            Optional<Integer> staff = assignmentDao.findActiveStaffId(c, complaintId);
            if (staff.isPresent())
                notifier.notify(c, staff.get(), complaintId, "Complaint #" + complaintId + " closed. Student rating: " + rating + "/5");
            return null;
        });
    }
}
