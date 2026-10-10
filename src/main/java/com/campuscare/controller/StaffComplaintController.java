package com.campuscare.controller;

import com.campuscare.model.Complaint;
import com.campuscare.model.Status;
import com.campuscare.service.ComplaintService;
import com.campuscare.service.StaffService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.ComplaintTable;
import com.campuscare.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;


public class StaffComplaintController {
    @FXML private TableView<Complaint> table;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextArea remarksArea;
    @FXML private Button acceptButton;
    @FXML private Button startButton;
    @FXML private Button resolveButton;
    @FXML private Button detailsButton;

    private final StaffService staffService = new StaffService();
    private final ComplaintService complaintService = new ComplaintService();
    private ComplaintTable complaintTable;

    @FXML
    public void initialize() {
        complaintTable = new ComplaintTable(table, searchField, statusFilter, priorityFilter, categoryFilter, true, false);
        table.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> updateButtons(c));
        updateButtons(null);
        refresh();
    }

    private void refresh() {
        complaintTable.setData(staffService.assignedTo(SessionManager.requireUser().id()));
        updateButtons(complaintTable.selected());
    }

    private void updateButtons(Complaint c) {
        String s = c == null ? "" : c.status();
        detailsButton.setDisable(c == null);
        acceptButton.setDisable(!Status.ASSIGNED.equals(s));
        startButton.setDisable(!(Status.ASSIGNED.equals(s) || Status.ACCEPTED.equals(s)));
        resolveButton.setDisable(!Status.IN_PROGRESS.equals(s));
    }

    @FXML private void onAccept() { act(staffService::accept, "Complaint accepted."); }
    @FXML private void onStart() { act(staffService::startWork, "Work started. Status: IN PROGRESS."); }
    @FXML private void onResolve() { act(staffService::resolve, "Complaint marked as RESOLVED. The student has been notified."); }

    private void act(StaffAction action, String successMessage) {
        Complaint c = complaintTable.selected();
        if (c == null) return;
        try {
            action.run(c.id(), SessionManager.requireUser().id(), remarksArea.getText());
            remarksArea.clear();
            AlertUtil.info(successMessage);
            refresh();
        } catch (AppException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void onDetails() {
        Complaint c = complaintTable.selected();
        if (c != null) Navigator.<ComplaintDetailsController>showDialog("ComplaintDetails.fxml",
                "Complaint #" + c.id(), ctrl -> ctrl.setComplaint(complaintService.getById(c.id())));
    }

    @FXML
    private void onRefresh() { refresh(); }

    @FunctionalInterface
    private interface StaffAction {
        void run(int complaintId, int staffId, String remarks);
    }
}
