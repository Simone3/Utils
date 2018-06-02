package com.utils.common.graphic;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * Wrapper for JavaFX GridPane to provide an easier interface for simple needs
 */
public class SimpleGrid extends GridPane {

    private final int columnNumber;
    private int currentRow = 0;

    /**
     * Constructor
     * @param columnNumber number of columns
     */
    public SimpleGrid(int columnNumber) {

        super();

        this.columnNumber = columnNumber;

        // Component settings
        setAlignment(Pos.CENTER);
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(25, 25, 25, 25));
        setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

        // Container columns constraints
        for(int i = 0; i < columnNumber; i++) {

            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth((double) 100 / columnNumber);
            getColumnConstraints().add(column);
        }
    }

    /**
     * Simple method that adds a new row. If just one value, it will take the whole column span.
     * @param columnValues values for all columns. Must have 1 <= length <= columnNumber
     */
    public void addNextSimpleRow(Node... columnValues) {

        if(columnValues == null || columnValues.length == 0) {

            throw new IllegalArgumentException("No column values");
        }

        if(columnValues.length > columnNumber) {

            throw new IllegalArgumentException("Too many column values");
        }

        int colSpan = (columnValues.length == 1 ? columnNumber : 1);
        for(int i = 0; i < columnValues.length; i++) {

            add(columnValues[i], i, currentRow, colSpan, 1);
        }

        currentRow++;
    }
}
