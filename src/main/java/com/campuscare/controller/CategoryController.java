package com.campuscare.controller;

import com.campuscare.model.Category;
import com.campuscare.model.Department;
import com.campuscare.service.AdminService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class CategoryController {
    @FXML private TableView<Category> table;
    @FXML private TextField nameField;
    @FXML private ComboBox<Department> departmentBox;

    private final AdminService admin = new AdminService();

    @FXML
    public void initialize() {
        UiUtil.setConverter(departmentBox, Department::name);
        departmentBox.setItems(FXCollections.observableArrayList(admin.departments()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(UiUtil.col("ID", c -> String.valueOf(c.id()), 50));
        table.getColumns().add(UiUtil.col("Category", Category::name, 200));
        table.getColumns().add(UiUtil.col("Responsible Department", Category::departmentName, 200));
        table.getSelectionModel().selectedItemProperty().addListener((o, a, c) -> {
            if (c == null) return;
            nameField.setText(c.name());
            departmentBox.getItems().stream().filter(d -> d.id() == c.departmentId()).findFirst()
                    .ifPresent(departmentBox::setValue);
        });
        refresh();
    }

    private void refresh() {
        table.setItems(FXCollections.observableArrayList(admin.categories()));
    }

    @FXML private void onAdd() { run(() -> admin.addCategory(nameField.getText(), departmentBox.getValue())); onClear(); }

    @FXML
    private void onUpdate() {
        Category c = table.getSelectionModel().getSelectedItem();
        if (c == null) { AlertUtil.warn("Select a category from the table."); return; }
        run(() -> admin.updateCategory(c.id(), nameField.getText(), departmentBox.getValue()));
    }

    @FXML
    private void onDelete() {
        Category c = table.getSelectionModel().getSelectedItem();
        if (c == null) { AlertUtil.warn("Select a category from the table."); return; }
        if (AlertUtil.confirm("Delete category '" + c.name() + "'?")) {
            run(() -> admin.deleteCategory(c.id()));
            onClear();
        }
    }

    @FXML
    private void onClear() {
        nameField.clear();
        departmentBox.setValue(null);
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
