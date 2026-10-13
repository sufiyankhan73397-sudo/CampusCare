package com.campuscare.controller;

import com.campuscare.model.Status;
import com.campuscare.service.AdminService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.util.Map;

/** Admin overview: summary cards + JavaFX charts. */
public class AnalyticsController {
    @FXML private Label totalLabel;
    @FXML private Label openLabel;
    @FXML private Label doneLabel;
    @FXML private Label ratingLabel;
    @FXML private Label usersLabel;
    @FXML private PieChart statusPie;
    @FXML private BarChart<String, Number> departmentChart;
    @FXML private BarChart<String, Number> priorityChart;
    @FXML private BarChart<String, Number> categoryChart;

    private final AdminService admin = new AdminService();

    @FXML
    public void initialize() {
        Map<String, Integer> byStatus = admin.byStatus();
        int total = byStatus.values().stream().mapToInt(Integer::intValue).sum();
        int done = byStatus.getOrDefault(Status.RESOLVED, 0) + byStatus.getOrDefault(Status.CLOSED, 0);
        totalLabel.setText(String.valueOf(total));
        doneLabel.setText(String.valueOf(done));
        openLabel.setText(String.valueOf(total - done));
        usersLabel.setText(String.valueOf(admin.totalUsers()));
        double[] rating = admin.overallRating();
        ratingLabel.setText(rating[1] == 0 ? "-" : String.format("%.1f / 5", rating[0]));

        statusPie.setAnimated(false);
        statusPie.setData(FXCollections.observableArrayList(byStatus.entrySet().stream()
                .map(e -> new PieChart.Data(Status.pretty(e.getKey()) + " (" + e.getValue() + ")", e.getValue()))
                .toList()));

        fill(departmentChart, admin.byDepartment());
        fill(priorityChart, admin.byPriority());
        fill(categoryChart, admin.byCategory());
    }

    private void fill(BarChart<String, Number> chart, Map<String, Integer> data) {
        chart.setAnimated(false);
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        data.forEach((k, v) -> series.getData().add(new XYChart.Data<>(Status.pretty(k), v)));
        chart.getData().add(series);
    }
}
