package com.campuscare.controller;

/** Admin portal: analytics, complaints/assignment, master data, users, feedback. */
public class AdminDashboardController extends BaseDashboardController {
    @Override
    protected String portalName() { return "Admin Portal"; }

    @Override
    protected void buildMenu() {
        addMenu("Overview", () -> open("Analytics.fxml", "Overview & Analytics"));
        addMenu("Complaints", () -> open("AdminComplaints.fxml", "All Complaints & Assignment"));
        addMenu("Users", () -> open("UserManagement.fxml", "User Management"));
        addMenu("Departments", () -> open("Department.fxml", "Department Management"));
        addMenu("Categories", () -> open("Category.fxml", "Category Management"));
        addMenu("Locations", () -> open("Location.fxml", "Location Management"));
        addMenu("Feedback", () -> open("Feedback.fxml", "Feedback & Ratings"));
        addNotificationMenu();
        addMenu("My Profile", () -> open("Profile.fxml", "My Profile"));
    }
}
