package com.campuscare.controller;

import com.campuscare.model.Category;
import com.campuscare.model.Complaint;
import com.campuscare.model.Location;
import com.campuscare.model.Status;
import com.campuscare.service.ComplaintService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.SessionManager;
import com.campuscare.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class SubmitComplaintController {
    @FXML private TextField titleField;
    @FXML private ComboBox<Category> categoryBox;
    @FXML private ComboBox<Location> locationBox;
    @FXML private ComboBox<String> priorityBox;
    @FXML private TextArea descriptionArea;
    @FXML private Label departmentLabel;
    @FXML private Label attachmentLabel;

    private final ComplaintService service = new ComplaintService();
    private File attachment;

    @FXML
    public void initialize() {
        UiUtil.setConverter(categoryBox, Category::name);
        UiUtil.setConverter(locationBox, Location::name);
        categoryBox.setItems(FXCollections.observableArrayList(service.categories()));
        locationBox.setItems(FXCollections.observableArrayList(service.locations()));
        priorityBox.setItems(FXCollections.observableArrayList(Status.PRIORITIES));
        priorityBox.setValue("MEDIUM");

        // Student never picks a department: it is shown here only for information.
        categoryBox.valueProperty().addListener((o, oldV, cat) -> departmentLabel.setText(
                cat == null ? "Department is assigned automatically from the category."
                        : "Department (automatic): " + cat.departmentName()));
    }

    @FXML
    private void onChooseFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select attachment (PNG, JPG, PDF - max 5 MB)");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images and PDF", "*.png", "*.jpg", "*.jpeg", "*.pdf"));
        File f = fc.showOpenDialog(titleField.getScene().getWindow());
        if (f != null) {
            attachment = f;
            attachmentLabel.setText(f.getName());
        }
    }

    @FXML
    private void onClearFile() {
        attachment = null;
        attachmentLabel.setText("No file selected");
    }

    @FXML
    private void onSubmit() {
        try {
            Complaint c = service.submit(SessionManager.requireUser().id(), categoryBox.getValue(),
                    locationBox.getValue(), titleField.getText(), descriptionArea.getText(),
                    priorityBox.getValue(), attachment);
            AlertUtil.info("Complaint #" + c.id() + " submitted.\nDepartment: " + c.departmentName()
                    + "\nStatus: " + Status.pretty(c.status())
                    + (c.assignedStaff() != null ? "\nAssigned to: " + c.assignedStaff() : ""));
            onReset();
        } catch (AppException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void onReset() {
        titleField.clear();
        descriptionArea.clear();
        categoryBox.setValue(null);
        locationBox.setValue(null);
        priorityBox.setValue("MEDIUM");
        onClearFile();
    }
}
