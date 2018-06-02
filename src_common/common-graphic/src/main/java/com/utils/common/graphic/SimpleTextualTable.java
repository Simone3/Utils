package com.utils.common.graphic;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;

/**
 * Wrapper for JavaFX TableView to provide an easier interface for simple needs
 * @param <S> The type of the objects contained within the TableView items list
 */
public class SimpleTextualTable<S> extends TableView<S> {

    private ObservableList<S> tableData;

    /**
     * Constructor
     */
    public SimpleTextualTable() {

        super();

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        tableData = FXCollections.observableArrayList();
        setItems(tableData);
    }

    /**
     * Allows to change the table columns
     * @return list of columns
     */
    public ObservableList<S> getData() {

        return tableData;
    }

    /**
     * Adds a simple textual column
     * @param label the header label
     * @param property the name of the data property to be extracted from each row
     */
    public void addSimpleTextColumn(String label, String property) {

        TableColumn<S, String> column = new TableColumn<>(label);
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        column.setCellFactory(param -> {

            TableCell<S, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(param.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        getColumns().add(column);
    }
}
