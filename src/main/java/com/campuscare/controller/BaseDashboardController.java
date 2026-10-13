package com.campuscare.controller;

import com.campuscare.model.User;
import com.campuscare.service.NotificationService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared shell (sidebar + top bar + content area) used by Student, Staff and Admin dashboards.
 * Each role only decides which menu items exist (buildMenu).
 */
public abstract class BaseDashboardController {
    @FXML protected VBox menuBox;
    @FXML protected StackPane content;
    @FXML protected Label userLabel;
    @FXML protected Label roleLabel;
    @FXML protected Label pageTitle;

    private final List<Button> menuButtons = new ArrayList<>();
    private final NotificationService notificationService = new NotificationService();
    private Button notificationButton;

    protected abstract String portalName();

    protected abstract void buildMenu();

    @FXML
    public void initialize() {
        User user = SessionManager.requireUser();
        userLabel.setText(user.name() + "\n" + user.email());
        roleLabel.setText(portalName());
        Navigator.setNotificationsListener(this::refreshBadge);
        buildMenu();
        if (!menuButtons.isEmpty()) menuButtons.get(0).fire();
    }

    protected Button addMenu(String text, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add("menu-button");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> {
            menuButtons.forEach(x -> x.getStyleClass().remove("menu-active"));
            b.getStyleClass().add("menu-active");
            try {
                action.run();
            } catch (AppException ex) {
                AlertUtil.error(ex.getMessage());
            }
            refreshBadge();
        });
        menuBox.getChildren().add(b);
        menuButtons.add(b);
        return b;
    }

    protected Button addNotificationMenu() {
        notificationButton = addMenu("Notifications", () -> open("Notifications.fxml", "Notifications"));
        return notificationButton;
    }

    /** Opens a screen in the content area. A missing/broken screen shows a note instead of crashing. */
    protected void open(String fxml, String title) {
        pageTitle.setText(title);
        try {
            Navigator.loadInto(content, fxml);
        } catch (AppException e) {
            Label note = new Label("This screen is not available yet.\n" + e.getMessage());
            note.getStyleClass().add("hint-label");
            content.getChildren().setAll(note);
        }
    }

    private void refreshBadge() {
        if (notificationButton == null) return;
        int unread = notificationService.unreadCount();
        notificationButton.setText(unread > 0 ? "Notifications (" + unread + ")" : "Notifications");
    }

    @FXML
    protected void onLogout() {
        SessionManager.logout();
        Navigator.setNotificationsListener(null);
        Navigator.showLogin();
    }
}
