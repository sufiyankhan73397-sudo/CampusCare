package com.campuscare.controller;

import com.campuscare.model.Notification;
import com.campuscare.service.NotificationService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

public class NotificationsController {
    @FXML private ListView<Notification> listView;

    private final NotificationService service = new NotificationService();

    @FXML
    public void initialize() {
        listView.setPlaceholder(new Label("No notifications yet."));
        listView.setCellFactory(lv -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                getStyleClass().remove("notif-unread");
                if (empty || n == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Label msg = new Label(n.message());
                msg.setWrapText(true);
                msg.maxWidthProperty().bind(listView.widthProperty().subtract(60));
                Label time = new Label(n.createdAt());
                time.getStyleClass().add("muted");
                setGraphic(new VBox(3, msg, time));
                setText(null);
                if (!n.read()) getStyleClass().add("notif-unread");
            }
        });
        listView.setOnMouseClicked(e -> {
            Notification n = listView.getSelectionModel().getSelectedItem();
            if (n != null && !n.read()) {
                service.markRead(n.id());
                load();
                Navigator.notificationsChanged();
            }
        });
        load();
    }

    private void load() {
        listView.setItems(FXCollections.observableArrayList(service.myNotifications()));
    }

    @FXML
    private void onMarkAllRead() {
        service.markAllRead();
        load();
        Navigator.notificationsChanged();
    }

    @FXML
    private void onRefresh() {
        load();
        Navigator.notificationsChanged();
    }
}
