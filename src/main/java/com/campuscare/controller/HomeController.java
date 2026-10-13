package com.campuscare.controller;

import com.campuscare.model.Complaint;
import com.campuscare.model.Status;
import com.campuscare.model.User;
import com.campuscare.service.ComplaintService;
import com.campuscare.util.SessionManager;
import com.campuscare.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;

import java.util.List;

/** Dashboard home for students and staff: 4 summary cards + 5 most recent complaints. */
public class HomeController {
    @FXML private Label welcomeLabel;
    @FXML private Label totalLabel;
    @FXML private Label pendingLabel;
    @FXML private Label progressLabel;
    @FXML private Label doneLabel;
    @FXML private TableView<Complaint> recentTable;

    private final ComplaintService complaintService = new ComplaintService();

    @FXML
    public void initialize() {
        User u = SessionManager.requireUser();
        welcomeLabel.setText("Welcome, " + u.name());
        List<Complaint> list = Status.ROLE_STAFF.equals(u.role())
                ? complaintService.forStaff(u.id())
                : complaintService.forStudent(u.id());

        totalLabel.setText(String.valueOf(list.size()));
        pendingLabel.setText(String.valueOf(count(list, Status.SUBMITTED, Status.ASSIGNED, Status.ACCEPTED, Status.REOPENED)));
        progressLabel.setText(String.valueOf(count(list, Status.IN_PROGRESS)));
        doneLabel.setText(String.valueOf(count(list, Status.RESOLVED, Status.CLOSED)));

        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        recentTable.getColumns().add(UiUtil.col("ID", c -> String.valueOf(c.id()), 50));
        recentTable.getColumns().add(UiUtil.col("Title", Complaint::title, 220));
        recentTable.getColumns().add(UiUtil.col("Category", Complaint::categoryName, 110));
        recentTable.getColumns().add(UiUtil.badgeCol("Status", Complaint::status, 110));
        recentTable.getColumns().add(UiUtil.col("Created", Complaint::createdAt, 140));
        recentTable.setItems(FXCollections.observableArrayList(list.stream().limit(5).toList()));
        recentTable.setPlaceholder(new Label("No complaints yet."));
    }

    private long count(List<Complaint> list, String... statuses) {
        List<String> wanted = List.of(statuses);
        return list.stream().filter(c -> wanted.contains(c.status())).count();
    }
}
