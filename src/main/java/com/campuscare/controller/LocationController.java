package com.campuscare.controller;

import com.campuscare.model.Location;
import com.campuscare.service.AdminService;
import com.campuscare.util.AlertUtil;
import com.campuscare.util.AppException;
import com.campuscare.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class LocationController {
    @FXML private TableView<Location> table;
    @FXML private TextField nameField;
    @FXML private TextField buildingField;
    @FXML private TextField floorField;
    @FXML private TextField descriptionField;

    private final AdminService admin = new AdminService();

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getColumns().add(UiUtil.col("ID", l -> String.valueOf(l.id()), 50));
        table.getColumns().add(UiUtil.col("Location", Location::name, 180));
        table.getColumns().add(UiUtil.col("Building", Location::building, 120));
        table.getColumns().add(UiUtil.col("Floor", Location::floor, 70));
        table.getColumns().add(UiUtil.col("Description", Location::description, 220));
        table.getSelectionModel().selectedItemProperty().addListener((o, a, l) -> {
            if (l == null) return;
            nameField.setText(l.name());
            buildingField.setText(UiUtil.nz(l.building()));
            floorField.setText(UiUtil.nz(l.floor()));
            descriptionField.setText(UiUtil.nz(l.description()));
        });
        refresh();
    }

    private void refresh() {
        table.setItems(FXCollections.observableArrayList(admin.locations()));
    }

    @FXML
    private void onAdd() {
        run(() -> admin.addLocation(nameField.getText(), buildingField.getText(), floorField.getText(), descriptionField.getText()));
        onClear();
    }

    @FXML
    private void onUpdate() {
        Location l = table.getSelectionModel().getSelectedItem();
        if (l == null) { AlertUtil.warn("Select a location from the table."); return; }
        run(() -> admin.updateLocation(l.id(), nameField.getText(), buildingField.getText(), floorField.getText(), descriptionField.getText()));
    }

    @FXML
    private void onDelete() {
        Location l = table.getSelectionModel().getSelectedItem();
        if (l == null) { AlertUtil.warn("Select a location from the table."); return; }
        if (AlertUtil.confirm("Delete location '" + l.name() + "'?")) {
            run(() -> admin.deleteLocation(l.id()));
            onClear();
        }
    }

    @FXML
    private void onClear() {
        nameField.clear();
        buildingField.clear();
        floorField.clear();
        descriptionField.clear();
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
