package com.campuscare;

import com.campuscare.config.AppConfig;
import com.campuscare.controller.Navigator;
import com.campuscare.database.DatabaseInitializer;
import com.campuscare.util.AlertUtil;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static HostServices hostServices;

    public static HostServices host() { return hostServices; }

    @Override
    public void start(Stage stage) {
        hostServices = getHostServices();

        // Any unexpected error shows a message instead of silently doing nothing.
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace();
            Platform.runLater(() -> AlertUtil.error("Unexpected error: " + e.getMessage()));
        });

        try {
            DatabaseInitializer.initialize();   // creates data/campuscare.db, tables and default data
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.error("Could not start CampusCare.\n" + e.getMessage());
            Platform.exit();
            return;
        }

        stage.setTitle(AppConfig.APP_TITLE);
        Navigator.init(stage);
        Navigator.showLogin();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
