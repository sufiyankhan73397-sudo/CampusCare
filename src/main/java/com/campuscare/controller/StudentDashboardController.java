package com.campuscare.controller;

/** Student portal: report issue, track complaints, notifications, profile. */
public class StudentDashboardController extends BaseDashboardController {
    @Override
    protected String portalName() { return "Student Portal"; }

    @Override
    protected void buildMenu() {
        addMenu("Dashboard", () -> open("Home.fxml", "Dashboard"));
        addMenu("Report Issue", () -> open("SubmitComplaint.fxml", "Report an Issue"));
        addMenu("My Complaints", () -> open("MyComplaints.fxml", "My Complaints"));
        addNotificationMenu();
        addMenu("My Profile", () -> open("Profile.fxml", "My Profile"));
    }
}
