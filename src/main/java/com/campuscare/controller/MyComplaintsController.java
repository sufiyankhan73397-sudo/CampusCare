package com.campuscare.controller;

import com.campuscare.model.Complaint;
import com.campuscare.model.Status;
import com.campuscare.service.ComplaintService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.ComplaintTable;
import com.campuscare.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class MyComplaintsController {
    @FXML private TableView<Complaint> table;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> priorityFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private Button detailsButton;
    @FXML private Button reopenButton;
    @FXML private Button feedbackButton;

    private final ComplaintService service = new ComplaintService();
    private ComplaintTable complaintTable;

    @FXML
    public void initialize() {
        complaintTable = new ComplaintTable(table, searchField, statusFilter, priorityFilter, categoryFilter, false, true);
        table.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> updateButtons(c));
        updateButtons(null);
        refresh();
    }

    private void refresh() {
        complaintTable.setData(service.forStudent(SessionManager.requireUser().id()));
        updateButtons(complaintTable.selected());
    }

    private void updateButtons(Complaint c) {
        boolean resolved = c != null && Status.RESOLVED.equals(c.status());
        detailsButton.setDisable(c == null);
        reopenButton.setDisable(!resolved);
        feedbackButton.setDisable(!resolved);
    }

    @FXML
    private void onDetails() {
        Complaint c = complaintTable.selected();
        if (c != null) Navigator.<ComplaintDetailsController>showDialog("ComplaintDetails.fxml",
                "Complaint #" + c.id(), ctrl -> ctrl.setComplaint(service.getById(c.id())));
    }

    @FXML
    private void onReopen() {
        Complaint c = complaintTable.selected();
        if (c == null) return;
        AlertUtil.prompt("Reopen complaint #" + c.id(), "Why is the issue not solved?").ifPresent(reason -> {
            try {
                service.reopen(c.id(), SessionManager.requireUser().id(), reason);
                AlertUtil.info("Complaint reopened and sent back to the department.");
                refresh();
            } catch (AppException e) {
                AlertUtil.error(e.getMessage());
            }
        });
    }

    @FXML
    private void onFeedback() {
        Complaint c = complaintTable.selected();
        if (c == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Feedback");
        dialog.setHeaderText("Rate the resolution of complaint #" + c.id());
        ComboBox<Integer> rating = new ComboBox<>(FXCollections.observableArrayList(5, 4, 3, 2, 1));
        rating.setValue(5);
        TextArea comments = new TextArea();
        comments.setPromptText("Comments (optional)");
        comments.setPrefRowCount(3);
        dialog.getDialogPane().setContent(new VBox(10, new Label("Rating (5 = excellent)"), rating,
                new Label("Comments"), comments));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.giveFeedback(c.id(), SessionManager.requireUser().id(), rating.getValue(), comments.getText());
                AlertUtil.info("Thank you for your feedback. The complaint is now CLOSED.");
                refresh();
            } catch (AppException e) {
                AlertUtil.error(e.getMessage());
            }
        }
    }

    @FXML
    private void onRefresh() { refresh(); }
}
