package com.campuscare.controller;

import com.campuscare.model.Complaint;
import com.campuscare.model.Status;
import com.campuscare.model.User;
import com.campuscare.service.AdminService;
import com.campuscare.service.ComplaintService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.ComplaintTable;
import com.campuscare.util.CsvUtil;
import com.campuscare.util.UiUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.Set;

public class AdminComplaintsController {
    private static final Set<String> ASSIGNABLE =
            Set.of(Status.SUBMITTED, Status.ASSIGNED, Status.ACCEPTED, Status.REOPENED);

    @FXML private TableView<Complaint> table;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ComboBox<User> staffBox;
    @FXML private Label departmentHint;
    @FXML private Button assignButton;
    @FXML private Button detailsButton;

    private final AdminService admin = new AdminService();
    private final ComplaintService complaintService = new ComplaintService();
    private ComplaintTable complaintTable;

    @FXML
    public void initialize() {
        complaintTable = new ComplaintTable(table, searchField, statusFilter, priorityFilter, categoryFilter, true, true);
        UiUtil.setConverter(staffBox, User::name);
        table.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> onSelect(c));
        onSelect(null);
        refresh();
    }

    private void refresh() {
        complaintTable.setData(admin.allComplaints());
        onSelect(complaintTable.selected());
    }

    private void onSelect(Complaint c) {
        staffBox.getItems().clear();
        detailsButton.setDisable(c == null);
        if (c == null) {
            departmentHint.setText("Select a complaint to assign staff.");
            assignButton.setDisable(true);
            return;
        }
        departmentHint.setText("Staff of " + c.departmentName() + " department:");
        staffBox.getItems().setAll(admin.staffOfDepartment(c.departmentId()));
        assignButton.setDisable(!ASSIGNABLE.contains(c.status()));
    }

    @FXML
    private void onAssign() {
        Complaint c = complaintTable.selected();
        User staff = staffBox.getValue();
        if (c == null) return;
        if (staff == null) {
            AlertUtil.warn("Select a staff member first.");
            return;
        }
        try {
            admin.assign(c.id(), staff.id());
            AlertUtil.info("Complaint #" + c.id() + " assigned to " + staff.name() + ".");
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
    private void onExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export complaints report");
        fc.setInitialFileName("campuscare-complaints.csv");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV file", "*.csv"));
        File f = fc.showSaveDialog(table.getScene().getWindow());
        if (f == null) return;
        try {
            CsvUtil.exportComplaints(complaintTable.visibleItems(), f.toPath());
            AlertUtil.info("Report saved:\n" + f.getAbsolutePath());
        } catch (AppException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void onRefresh() { refresh(); }
}
