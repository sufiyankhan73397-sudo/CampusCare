package com.campuscare.service;

import com.campuscare.dao.AnalyticsDao;
import com.campuscare.dao.CategoryDao;
import com.campuscare.dao.ComplaintDao;
import com.campuscare.dao.DepartmentDao;
import com.campuscare.dao.FeedbackDao;
import com.campuscare.dao.LocationDao;
import com.campuscare.dao.UserDao;
import com.campuscare.database.DatabaseConnection;
import com.campuscare.model.Category;
import com.campuscare.model.Complaint;
import com.campuscare.model.Department;
import com.campuscare.model.DepartmentRating;
import com.campuscare.model.FeedbackRow;
import com.campuscare.model.Location;
import com.campuscare.model.Status;
import com.campuscare.model.User;
import com.campuscare.util.AppException;
import com.campuscare.util.CsvUtil;
import com.campuscare.util.SessionManager;
import com.campuscare.util.ValidationUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Modules M6 + M7: admin management, assignment, analytics, feedback view, reports. */
public class AdminService {
    private final UserDao userDao = new UserDao();
    private final DepartmentDao departmentDao = new DepartmentDao();
    private final CategoryDao categoryDao = new CategoryDao();
    private final LocationDao locationDao = new LocationDao();
    private final ComplaintDao complaintDao = new ComplaintDao();
    private final FeedbackDao feedbackDao = new FeedbackDao();
    private final AnalyticsDao analyticsDao = new AnalyticsDao();
    private final AuthService authService = new AuthService();
    private final NotificationService notifier = new NotificationService();
    private final WorkflowHelper workflow = new WorkflowHelper();

    private void requireAdmin() { SessionManager.requireRole(Status.ROLE_ADMIN); }

    // ---------- users ----------
    public List<User> users() { requireAdmin(); return userDao.findAll(); }

    public void addUser(String name, String email, String phone, String password, String role, Integer departmentId) {
        requireAdmin();
        authService.createUser(name, email, phone, password, role, departmentId);
    }

    public void setUserStatus(int userId, boolean active) {
        requireAdmin();
        if (userId == SessionManager.requireUser().id()) throw new AppException("You cannot deactivate your own account.");
        userDao.updateStatus(userId, active ? "ACTIVE" : "INACTIVE");
    }

    // ---------- departments ----------
    public List<Department> departments() { return departmentDao.findAll(); }

    public void addDepartment(String name, String description) {
        requireAdmin();
        ValidationUtil.checkLength("Department name", name, 2, 50);
        departmentDao.insert(name.trim(), ValidationUtil.clean(description));
    }

    public void updateDepartment(int id, String name, String description) {
        requireAdmin();
        ValidationUtil.checkLength("Department name", name, 2, 50);
        departmentDao.update(id, name.trim(), ValidationUtil.clean(description));
    }

    public void setDepartmentActive(int id, boolean active) {
        requireAdmin();
        departmentDao.updateStatus(id, active ? "ACTIVE" : "INACTIVE");
    }

    // ---------- categories ----------
    public List<Category> categories() { return categoryDao.findAll(); }

    public void addCategory(String name, Department dept) {
        requireAdmin();
        ValidationUtil.checkLength("Category name", name, 2, 50);
        if (dept == null) throw new AppException("Select a department for this category.");
        categoryDao.insert(name.trim(), dept.id());
    }

    public void updateCategory(int id, String name, Department dept) {
        requireAdmin();
        ValidationUtil.checkLength("Category name", name, 2, 50);
        if (dept == null) throw new AppException("Select a department for this category.");
        categoryDao.update(id, name.trim(), dept.id());
    }

    public void deleteCategory(int id) { requireAdmin(); categoryDao.delete(id); }

    // ---------- locations ----------
    public List<Location> locations() { return locationDao.findAll(); }

    public void addLocation(String name, String building, String floor, String description) {
        requireAdmin();
        ValidationUtil.checkLength("Location name", name, 2, 60);
        locationDao.insert(name.trim(), ValidationUtil.clean(building), ValidationUtil.clean(floor), ValidationUtil.clean(description));
    }

    public void updateLocation(int id, String name, String building, String floor, String description) {
        requireAdmin();
        ValidationUtil.checkLength("Location name", name, 2, 60);
        locationDao.update(id, name.trim(), ValidationUtil.clean(building), ValidationUtil.clean(floor), ValidationUtil.clean(description));
    }

    public void deleteLocation(int id) { requireAdmin(); locationDao.delete(id); }

    // ---------- complaints + assignment ----------
    public List<Complaint> allComplaints() { requireAdmin(); return complaintDao.findAll(); }

    public List<User> staffOfDepartment(int departmentId) {
        requireAdmin();
        return userDao.findActiveStaffByDepartment(departmentId);
    }

    public void assign(int complaintId, int staffId) {
        requireAdmin();
        int adminId = SessionManager.requireUser().id();
        DatabaseConnection.tx(c -> {
            Complaint cp = complaintDao.findById(c, complaintId).orElseThrow(() -> new AppException("Complaint not found."));
            if (!Set.of(Status.SUBMITTED, Status.ASSIGNED, Status.ACCEPTED, Status.REOPENED).contains(cp.status()))
                throw new AppException("A complaint with status " + Status.pretty(cp.status()) + " cannot be (re)assigned.");
            if (!userDao.isActiveStaffOfDepartment(c, staffId, cp.departmentId()))
                throw new AppException("Selected staff does not belong to the " + cp.departmentName() + " department.");
            String staffName = userDao.findNameById(c, staffId);
            workflow.assign(c, cp, staffId, adminId, "Assigned by admin to " + staffName);
            return null;
        });
    }

    public void exportComplaintsCsv(Path file) {
        requireAdmin();
        CsvUtil.exportComplaints(complaintDao.findAll(), file);
    }

    // ---------- analytics + feedback ----------
    public Map<String, Integer> byStatus() { return analyticsDao.countByStatus(); }
    public Map<String, Integer> byPriority() { return analyticsDao.countByPriority(); }
    public Map<String, Integer> byDepartment() { return analyticsDao.countByDepartment(); }
    public Map<String, Integer> byCategory() { return analyticsDao.countByCategory(); }
    public int totalUsers() { return analyticsDao.totalUsers(); }
    public int totalStaff() { return analyticsDao.totalStaff(); }

    public List<FeedbackRow> feedbackRows() { return feedbackDao.findAll(); }
    public List<DepartmentRating> departmentRatings() { return feedbackDao.averageByDepartment(); }
    /** {average, total} */
    public double[] overallRating() { return feedbackDao.overall(); }
}
