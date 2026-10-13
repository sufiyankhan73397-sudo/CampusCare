package com.campuscare.controller;

import com.campuscare.model.User;
import com.campuscare.service.AuthService;
import com.campuscare.util.AppException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        try {
            User user = authService.login(emailField.getText(), passwordField.getText());
            Navigator.showDashboard(user);
        } catch (AppException e) {
            errorLabel.setText(e.getMessage());
            passwordField.clear();
        }
    }

    @FXML
    private void onRegister() { Navigator.showRegister(); }
}
