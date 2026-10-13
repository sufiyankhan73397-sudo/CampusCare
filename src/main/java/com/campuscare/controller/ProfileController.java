package com.campuscare.controller;

import com.campuscare.model.User;
import com.campuscare.service.AuthService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.SessionManager;
import com.campuscare.util.UiUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ProfileController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label roleLabel;
    @FXML private Label departmentLabel;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private final AuthService authService = new AuthService();

    @FXML
    public void initialize() {
        User u = SessionManager.requireUser();
        nameField.setText(u.name());
        emailField.setText(u.email());
        phoneField.setText(UiUtil.nz(u.phone()));
        roleLabel.setText(u.role());
        departmentLabel.setText(u.departmentName() == null ? "-" : u.departmentName());
    }

    @FXML
    private void onSaveProfile() {
        try {
            authService.updateProfile(nameField.getText(), phoneField.getText());
            AlertUtil.info("Profile updated.");
        } catch (AppException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML
    private void onChangePassword() {
        try {
            authService.changePassword(oldPasswordField.getText(), newPasswordField.getText(), confirmPasswordField.getText());
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();
            AlertUtil.info("Password changed successfully.");
        } catch (AppException e) {
            AlertUtil.error(e.getMessage());
        }
    }
}
