package com.campuscare.controller;

import com.campuscare.service.AuthService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterController {
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onRegister() {
        errorLabel.setText("");
        try {
            authService.register(nameField.getText(), emailField.getText(), phoneField.getText(),
                    passwordField.getText(), confirmField.getText());
            AlertUtil.info("Registration successful! You can login now.");
            Navigator.showLogin();
        } catch (AppException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onBack() { Navigator.showLogin(); }
}
