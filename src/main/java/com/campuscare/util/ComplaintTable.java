package com.campuscare.util;

import com.campuscare.model.Complaint;
import com.campuscare.model.Status;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Reusable complaint table with search, status / priority / category filters and column sorting.
 * Used by My Complaints (student), Assigned Complaints (staff) and All Complaints (admin).
 */
public class ComplaintTable {
    private static final String ALL = "All";

    private final TableView<Complaint> table;
    private final ObservableList<Complaint> master = FXCollections.observableArrayList();
    private final FilteredList<Complaint> filtered = new FilteredList<>(master, c -> true);
    private final TextField searchField;
    private final ComboBox<String> statusBox;
    private final ComboBox<String> priorityBox;
    private final ComboBox<String> categoryBox;

    public ComplaintTable(TableView<Complaint> table, TextField searchField, ComboBox<String> statusBox,
                          ComboBox<String> priorityBox, ComboBox<String> categoryBox,
                          boolean showStudent, boolean showStaff) {
        this.table = table;
        this.searchField = searchField;
        this.statusBox = statusBox;
        this.priorityBox = priorityBox;
        this.categoryBox = categoryBox;

        buildColumns(showStudent, showStaff);

        statusBox.getItems().add(ALL);
        statusBox.getItems().addAll(Status.ALL);
        statusBox.setValue(ALL);
        UiUtil.setConverter(statusBox, Status::pretty);
        priorityBox.getItems().add(ALL);
        priorityBox.getItems().addAll(Status.PRIORITIES);
        priorityBox.setValue(ALL);
        categoryBox.getItems().add(ALL);
        categoryBox.setValue(ALL);

        searchField.textProperty().addListener((o, a, b) -> applyFilter());
        statusBox.valueProperty().addListener((o, a, b) -> applyFilter());
        priorityBox.valueProperty().addListener((o, a, b) -> applyFilter());
        categoryBox.valueProperty().addListener((o, a, b) -> applyFilter());

        SortedList<Complaint> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sorted);
        table.setPlaceholder(new Label("No complaints found."));
    }

    private void buildColumns(boolean showStudent, boolean showStaff) {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Complaint, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().id()));
        idCol.setPrefWidth(50);
        table.getColumns().add(idCol);

        table.getColumns().add(UiUtil.col("Title", Complaint::title, 200));
        if (showStudent) table.getColumns().add(UiUtil.col("Student", Complaint::studentName, 110));
        table.getColumns().add(UiUtil.col("Category", Complaint::categoryName, 100));
        table.getColumns().add(UiUtil.col("Department", Complaint::departmentName, 100));
        table.getColumns().add(UiUtil.col("Location", Complaint::locationName, 110));
        table.getColumns().add(UiUtil.badgeCol("Priority", Complaint::priority, 90));
        table.getColumns().add(UiUtil.badgeCol("Status", Complaint::status, 110));
        if (showStaff) table.getColumns().add(UiUtil.col("Assigned To", Complaint::assignedStaff, 110));
        table.getColumns().add(UiUtil.col("Created", Complaint::createdAt, 130));
    }

    public void setData(List<Complaint> list) {
        Complaint previous = table.getSelectionModel().getSelectedItem();
        master.setAll(list);

        List<String> categories = list.stream().map(Complaint::categoryName).distinct().sorted().toList();
        String chosen = categoryBox.getValue();
        categoryBox.getItems().setAll(ALL);
        categoryBox.getItems().addAll(categories);
        categoryBox.setValue(chosen != null && categories.contains(chosen) ? chosen : ALL);

        if (previous != null) {
            for (Complaint c : table.getItems()) {
                if (c.id() == previous.id()) {
                    table.getSelectionModel().select(c);
                    break;
                }
            }
        }
    }

    public Complaint selected() {
        return table.getSelectionModel().getSelectedItem();
    }

    public List<Complaint> visibleItems() {
        return List.copyOf(filtered);
    }

    private void applyFilter() {
        String q = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String st = statusBox.getValue();
        String pr = priorityBox.getValue();
        String ca = categoryBox.getValue();
        filtered.setPredicate(c ->
                (q.isEmpty() || contains(c.title(), q) || contains(c.locationName(), q)
                        || contains(c.categoryName(), q) || contains(c.studentName(), q)
                        || String.valueOf(c.id()).equals(q))
                && (st == null || ALL.equals(st) || st.equals(c.status()))
                && (pr == null || ALL.equals(pr) || pr.equals(c.priority()))
                && (ca == null || ALL.equals(ca) || ca.equals(c.categoryName())));
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase().contains(q);
    }
}
