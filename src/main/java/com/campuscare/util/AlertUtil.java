package com.campuscare.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public final class AlertUtil {
    private AlertUtil() { }

    public static void info(String message) { show(Alert.AlertType.INFORMATION, "Information", message); }
    public static void error(String message) { show(Alert.AlertType.ERROR, "Error", message); }
    public static void warn(String message) { show(Alert.AlertType.WARNING, "Warning", message); }

    public static boolean confirm(String message) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        a.setTitle("Confirm");
        a.setHeaderText(null);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    public static Optional<String> prompt(String title, String label) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(title);
        d.setHeaderText(null);
        d.setContentText(label);
        return d.showAndWait();
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
