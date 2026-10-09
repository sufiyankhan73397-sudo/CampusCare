package com.campuscare.controller;

import com.campuscare.model.Status;
import com.campuscare.model.User;
import com.campuscare.util.AppException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.function.Consumer;

/** Loads FXML screens, switches between login/register/dashboards and opens dialogs. */
public final class Navigator {
    private static final String[] STYLES = {"/css/main.css", "/css/login.css", "/css/dashboard.css", "/css/table.css"};

    private static Stage stage;
    private static Scene scene;
    private static Runnable notificationsListener;

    private Navigator() { }

    public static void init(Stage primary) {
        stage = primary;
        scene = new Scene(new StackPane(), 1200, 740);
        addStyles(scene);
        stage.setScene(scene);
        stage.setMinWidth(1050);
        stage.setMinHeight(660);
    }

    public static void showLogin() { setRoot("Login.fxml"); }

    public static void showRegister() { setRoot("Register.fxml"); }

    /** After login the correct dashboard opens automatically according to the user's role. */
    public static void showDashboard(User user) {
        BaseDashboardController controller = switch (user.role()) {
            case Status.ROLE_ADMIN -> new AdminDashboardController();
            case Status.ROLE_STAFF -> new StaffDashboardController();
            default -> new StudentDashboardController();
        };
        try {
            FXMLLoader loader = loader("Dashboard.fxml");
            loader.setController(controller);
            scene.setRoot(loader.load());
        } catch (IOException e) {
            throw new AppException("Cannot open dashboard: " + e.getMessage(), e);
        }
    }

    private static void setRoot(String fxml) {
        try {
            scene.setRoot(loader(fxml).load());
        } catch (IOException e) {
            throw new AppException("Cannot open screen " + fxml + ": " + e.getMessage(), e);
        }
    }

    /** Loads a view into a container (the dashboard content area) and returns its controller. */
    public static <T> T loadInto(Pane container, String fxml) {
        try {
            FXMLLoader loader = loader(fxml);
            Parent root = loader.load();
            container.getChildren().setAll(root);
            return loader.getController();
        } catch (IOException e) {
            throw new AppException("Cannot open screen " + fxml + ": " + e.getMessage(), e);
        }
    }

    /** Opens a modal window; init receives the controller so data can be passed in. */
    public static <T> void showDialog(String fxml, String title, Consumer<T> init) {
        try {
            FXMLLoader loader = loader(fxml);
            Parent root = loader.load();
            T controller = loader.getController();
            if (init != null) init.accept(controller);
            Stage dialog = new Stage();
            dialog.initOwner(stage);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(title);
            Scene s = new Scene(root);
            addStyles(s);
            dialog.setScene(s);
            dialog.showAndWait();
        } catch (IOException e) {
            throw new AppException("Cannot open screen " + fxml + ": " + e.getMessage(), e);
        }
    }

    public static Stage getStage() { return stage; }

    // The dashboard registers a listener so the "Notifications (n)" badge updates after reading.
    public static void setNotificationsListener(Runnable r) { notificationsListener = r; }
    public static void notificationsChanged() { if (notificationsListener != null) notificationsListener.run(); }

    private static FXMLLoader loader(String fxml) {
        return new FXMLLoader(Navigator.class.getResource("/fxml/" + fxml));
    }

    private static void addStyles(Scene s) {
        for (String css : STYLES) s.getStylesheets().add(Navigator.class.getResource(css).toExternalForm());
    }
}
