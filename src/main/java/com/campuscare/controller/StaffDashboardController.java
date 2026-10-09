package com.campuscare.controller;

/** Staff portal: assigned complaints and the resolution workflow. */
public class StaffDashboardController extends BaseDashboardController {
    @Override
    protected String portalName() { return "Staff Portal"; }

    @Override
    protected void buildMenu() {
        addMenu("Dashboard", () -> open("Home.fxml", "Dashboard"));
        addMenu("Assigned Complaints", () -> open("StaffComplaint.fxml", "Assigned Complaints"));
        addNotificationMenu();
        addMenu("My Profile", () -> open("Profile.fxml", "My Profile"));
    }
}
