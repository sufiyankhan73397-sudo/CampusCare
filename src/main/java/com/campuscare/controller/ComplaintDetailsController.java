package com.campuscare.controller;

import com.campuscare.MainApp;
import com.campuscare.model.Attachment;
import com.campuscare.model.Complaint;
import com.campuscare.model.HistoryEntry;
import com.campuscare.model.Status;
import com.campuscare.service.ComplaintService;
import com.campuscare.util.UiUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.nio.file.Path;

/** Shows names (not ids), attachments and the DYNAMIC timeline read from complaint_history. */
public class ComplaintDetailsController {
    @FXML private Label titleLabel;
    @FXML private HBox badgeBox;
    @FXML private GridPane infoGrid;
    @FXML private Label descriptionLabel;
    @FXML private VBox attachmentBox;
    @FXML private VBox timelineBox;

    private final ComplaintService service = new ComplaintService();

    public void setComplaint(Complaint c) {
        titleLabel.setText("#" + c.id() + "  " + c.title());
        badgeBox.getChildren().setAll(UiUtil.badge(c.status()), UiUtil.badge(c.priority()));

        infoGrid.getChildren().clear();
        int r = 0;
        r = addRow(r, "Student", c.studentName());
        r = addRow(r, "Category", c.categoryName());
        r = addRow(r, "Department", c.departmentName());
        r = addRow(r, "Location", c.locationName());
        r = addRow(r, "Assigned To", c.assignedStaff() == null ? "Not assigned yet" : c.assignedStaff());
        r = addRow(r, "Created", c.createdAt());
        r = addRow(r, "Last Updated", c.updatedAt());
        addRow(r, "Resolved", c.resolvedAt() == null ? "-" : c.resolvedAt());

        descriptionLabel.setText(c.description());

        attachmentBox.getChildren().clear();
        var files = service.attachments(c.id());
        if (files.isEmpty()) attachmentBox.getChildren().add(new Label("No attachments"));
        for (Attachment a : files) {
            Hyperlink link = new Hyperlink(a.fileName());
            link.setOnAction(e -> MainApp.host().showDocument(Path.of(a.filePath()).toUri().toString()));
            attachmentBox.getChildren().add(link);
        }

        timelineBox.getChildren().clear();
        for (HistoryEntry h : service.history(c.id())) timelineBox.getChildren().add(timelineRow(h));
    }

    private int addRow(int row, String label, String value) {
        Label l = new Label(label);
        l.getStyleClass().add("info-key");
        Label v = new Label(UiUtil.nz(value));
        v.setWrapText(true);
        infoGrid.add(l, 0, row);
        infoGrid.add(v, 1, row);
        return row + 1;
    }

    private HBox timelineRow(HistoryEntry h) {
        Region dot = new Region();
        dot.getStyleClass().add("timeline-dot");

        Label head = new Label(Status.pretty(h.newStatus()));
        head.getStyleClass().add("timeline-status");
        Label meta = new Label(h.updatedAt() + "  |  " + h.updatedByName());
        meta.getStyleClass().add("muted");
        VBox text = new VBox(2, head, meta);
        if (h.remarks() != null && !h.remarks().isBlank()) {
            Label remarks = new Label(h.remarks());
            remarks.setWrapText(true);
            remarks.setMaxWidth(560);
            text.getChildren().add(remarks);
        }
        HBox row = new HBox(12, dot, text);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("timeline-row");
        return row;
    }

    @FXML
    private void onClose() {
        titleLabel.getScene().getWindow().hide();
    }
}
