package com.campuscare.util;

import com.campuscare.model.Status;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.StringConverter;

import java.util.function.Function;

/** Small helpers so TableView / ComboBox code in controllers stays short. */
public final class UiUtil {
    private UiUtil() { }

    public static <T> TableColumn<T, String> col(String title, Function<T, String> getter, double width) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> new SimpleStringProperty(nz(getter.apply(cd.getValue()))));
        c.setPrefWidth(width);
        return c;
    }

    public static <T> TableColumn<T, String> badgeCol(String title, Function<T, String> getter, double width) {
        TableColumn<T, String> c = col(title, getter, width);
        c.setCellFactory(tc -> new TableCell<T, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    setText(null);
                    setGraphic(badge(item));
                }
            }
        });
        return c;
    }

    public static Label badge(String value) {
        Label l = new Label(Status.pretty(value));
        l.getStyleClass().addAll("badge", "badge-" + value.toLowerCase().replace('_', '-'));
        return l;
    }

    public static <T> void setConverter(ComboBox<T> box, Function<T, String> toText) {
        box.setConverter(new StringConverter<T>() {
            @Override public String toString(T obj) { return obj == null ? "" : toText.apply(obj); }
            @Override public T fromString(String s) { return null; }
        });
    }

    public static String nz(String s) { return s == null ? "" : s; }
}
