package com.campuscare.controller;

import com.campuscare.model.DepartmentRating;
import com.campuscare.model.FeedbackRow;
import com.campuscare.service.AdminService;
import com.campuscare.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

public class FeedbackController {
    @FXML private Label averageLabel;
    @FXML private Label totalLabel;
    @FXML private TableView<DepartmentRating> departmentTable;
    @FXML private TableView<FeedbackRow> feedbackTable;

    private final AdminService admin = new AdminService();

    @FXML
    public void initialize() {
        double[] overall = admin.overallRating();
        averageLabel.setText(overall[1] == 0 ? "-" : String.format("%.2f / 5", overall[0]));
        totalLabel.setText(String.valueOf((int) overall[1]));

        departmentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        departmentTable.getColumns().add(UiUtil.col("Department", DepartmentRating::department, 140));
        departmentTable.getColumns().add(UiUtil.col("Average", r -> String.format("%.2f", r.average()), 80));
        departmentTable.getColumns().add(UiUtil.col("Feedback", r -> String.valueOf(r.count()), 80));
        departmentTable.setItems(FXCollections.observableArrayList(admin.departmentRatings()));

        feedbackTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        feedbackTable.getColumns().add(UiUtil.col("ID", f -> String.valueOf(f.complaintId()), 45));
        feedbackTable.getColumns().add(UiUtil.col("Complaint", FeedbackRow::title, 170));
        feedbackTable.getColumns().add(UiUtil.col("Student", FeedbackRow::studentName, 110));
        feedbackTable.getColumns().add(UiUtil.col("Department", FeedbackRow::departmentName, 100));
        feedbackTable.getColumns().add(UiUtil.col("Rating", f -> f.rating() + " / 5", 65));
        feedbackTable.getColumns().add(UiUtil.col("Comments", FeedbackRow::comments, 200));
        feedbackTable.getColumns().add(UiUtil.col("Date", FeedbackRow::createdAt, 130));
        feedbackTable.setItems(FXCollections.observableArrayList(admin.feedbackRows()));
    }
}
