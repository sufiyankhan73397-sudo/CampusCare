package com.campuscare.controller;

import com.campuscare.model.Department;
import com.campuscare.service.AdminService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class DepartmentController {
    @FXML private TableView<Department> table;
    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private Button toggleButton;

    private final AdminService admin = new AdminService();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(UiUtil.col("ID", d -> String.valueOf(d.id()), 50));
        table.getColumns().add(UiUtil.col("Department", Department::name, 160));
        table.getColumns().add(UiUtil.col("Description", Department::description, 320));
        table.getColumns().add(UiUtil.badgeCol("Status", Department::status, 90));
        table.getSelectionModel().selectedItemProperty().addListener((o, a, d) -> {
            if (d != null) {
                nameField.setText(d.name());
                descriptionField.setText(UiUtil.nz(d.description()));
            }
            toggleButton.setDisable(d == null);
            toggleButton.setText(d != null && "INACTIVE".equals(d.status()) ? "Activate" : "Deactivate");
        });
        toggleButton.setDisable(true);
        refresh();
    }

    private void refresh() {
        table.setItems(FXCollections.observableArrayList(admin.departments()));
    }

    @FXML
    private void onAdd() {
        run(() -> admin.addDepartment(nameField.getText(), descriptionField.getText()));
        onClear();
    }

    @FXML
    private void onUpdate() {
        Department d = table.getSelectionModel().getSelectedItem();
        if (d == null) { AlertUtil.warn("Select a department from the table."); return; }
        run(() -> admin.updateDepartment(d.id(), nameField.getText(), descriptionField.getText()));
    }

    @FXML
    private void onToggle() {
        Department d = table.getSelectionModel().getSelectedItem();
        if (d == null) return;
        run(() -> admin.setDepartmentActive(d.id(), "INACTIVE".equals(d.status())));
    }

    @FXML
    private void onClear() {
        nameField.clear();
        descriptionField.clear();
        table.getSelectionModel().clearSelection();
    }

    private void run(Runnable action) {
        try {
            action.run();
            refresh();
        } catch (AppException e) {
            AlertUtil.error(e.getMessage());
        }
    }
}
