package com.campuscare.controller;

import com.campuscare.model.Department;
import com.campuscare.model.Status;
import com.campuscare.model.User;
import com.campuscare.service.AdminService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class UserManagementController {
    @FXML private TableView<User> table;
    @FXML private Button toggleButton;

    private final AdminService admin = new AdminService();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(UiUtil.col("ID", u -> String.valueOf(u.id()), 45));
        table.getColumns().add(UiUtil.col("Name", User::name, 140));
        table.getColumns().add(UiUtil.col("Email", User::email, 190));
        table.getColumns().add(UiUtil.col("Phone", User::phone, 100));
        table.getColumns().add(UiUtil.badgeCol("Role", User::role, 90));
        table.getColumns().add(UiUtil.col("Department", User::departmentName, 100));
        table.getColumns().add(UiUtil.badgeCol("Status", User::status, 90));
        table.getColumns().add(UiUtil.col("Joined", User::createdAt, 130));
        table.getSelectionModel().selectedItemProperty().addListener((o, a, u) -> updateToggle(u));
        updateToggle(null);
        refresh();
    }

    private void refresh() {
        User prev = table.getSelectionModel().getSelectedItem();
        table.setItems(FXCollections.observableArrayList(admin.users()));
        if (prev != null) table.getItems().stream().filter(u -> u.id() == prev.id()).findFirst()
                .ifPresent(u -> table.getSelectionModel().select(u));
        updateToggle(table.getSelectionModel().getSelectedItem());
    }

    private void updateToggle(User u) {
        toggleButton.setDisable(u == null);
        toggleButton.setText(u != null && "INACTIVE".equals(u.status()) ? "Activate User" : "Deactivate User");
    }

    @FXML
    private void onToggleStatus() {
        User u = table.getSelectionModel().getSelectedItem();
        if (u == null) return;
        boolean activate = "INACTIVE".equals(u.status());
        if (!AlertUtil.confirm((activate ? "Activate " : "Deactivate ") + u.name() + "?")) return;
        try {
            admin.setUserStatus(u.id(), activate);
            refresh();
        } catch (AppException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void onAddUser() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Create a staff, admin or student account");

        TextField name = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();
        PasswordField password = new PasswordField();
        ComboBox<String> role = new ComboBox<>(FXCollections.observableArrayList(
                Status.ROLE_STAFF, Status.ROLE_ADMIN, Status.ROLE_STUDENT));
        role.setValue(Status.ROLE_STAFF);
        ComboBox<Department> department = new ComboBox<>(FXCollections.observableArrayList(admin.departments()));
        department.disableProperty().bind(role.valueProperty().isNotEqualTo(Status.ROLE_STAFF));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(15));
        grid.addRow(0, new Label("Name"), name);
        grid.addRow(1, new Label("Email"), email);
        grid.addRow(2, new Label("Phone"), phone);
        grid.addRow(3, new Label("Password"), password);
        grid.addRow(4, new Label("Role"), role);
        grid.addRow(5, new Label("Department"), department);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validate when OK is pressed; keep the dialog open if something is wrong.
        Button ok = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                Department d = department.getValue();
                admin.addUser(name.getText(), email.getText(), phone.getText(), password.getText(),
                        role.getValue(), d == null ? null : d.id());
            } catch (AppException ex) {
                AlertUtil.error(ex.getMessage());
                ev.consume();
            }
        });

        if (dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            refresh();
            AlertUtil.info("User created.");
        }
    }

    @FXML
    private void onRefresh() { refresh(); }
}
