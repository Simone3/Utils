package com.utils;

import com.utils.common.graphic.NumberTextField;
import com.utils.common.graphic.SimpleGrid;
import com.utils.common.graphic.SimpleTextualTable;
import com.utils.zipfiles.ZipFilesInFolder;
import com.utils.zipfiles.ZipResult;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import java.util.List;

public class Main extends Application {

    private final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        try {

            logger.log(Level.INFO, "Main.start");

            ZipFilesInFolder zipUtil = new ZipFilesInFolder();

            // Status message
            final Text status = new Text("");

            // Folder input
            Label folderLabel = new Label("Folder");
            final TextField folderInput = new TextField(System.getProperty("user.home") + "\\Desktop\\files");













            // TODO add output folder (create if not av) and test
























            // Prefix input
            Label prefixLabel = new Label("ZIP prefix");
            final TextField prefixInput = new TextField();
            prefixInput.setPromptText("Original file name");

            // Number input
            Label numberLabel = new Label("Files per ZIP");
            final NumberTextField numberInput = new NumberTextField(1);

            // Preview table
            final SimpleTextualTable<ZipResult> table = new SimpleTextualTable<>();
            table.addSimpleTextColumn("ZIP", "zipName");
            table.addSimpleTextColumn("Files", "containedFilesString");

            // Confirm button
            final Button confirmButton = new Button();
            confirmButton.setText("Confirm");
            confirmButton.setDisable(true);
            confirmButton.setOnAction(event -> {

                try {

                    zipUtil.process(folderInput.getText(), numberInput.getNumber(), prefixInput.getText(), true);

                    status.setText("OK!");
                }
                catch(IllegalStateException e) {

                    status.setText("Error: " + e.getMessage());
                }
            });

            // Preview button
            final Button previewButton = new Button();
            previewButton.setText("Preview");
            previewButton.setOnAction(event -> {

                status.setText("Processing...");

                try {

                    List<ZipResult> preview = zipUtil.process(folderInput.getText(), numberInput.getNumber(), prefixInput.getText(), false);

                    table.getData().clear();
                    table.getData().addAll(preview);

                    confirmButton.setDisable(false);

                    status.setText("Preview completed");
                }
                catch(IllegalStateException e) {

                    status.setText("Error: " + e.getMessage());
                    confirmButton.setDisable(true);
                }
            });

            // Container
            SimpleGrid root = new SimpleGrid(2);

            // Add components to container
            root.addNextSimpleRow(status);
            root.addNextSimpleRow(folderLabel, folderInput);
            root.addNextSimpleRow(prefixLabel, prefixInput);
            root.addNextSimpleRow(numberLabel, numberInput);
            root.addNextSimpleRow(previewButton, confirmButton);
            root.addNextSimpleRow(table);

            // Setup stage
            primaryStage.setTitle("Zip Files In Folder");
            primaryStage.setScene(new Scene(root, 500, 500));
            primaryStage.show();
        }
        catch(Exception e) {

            logger.log(Level.ERROR, "Uncaught exception in Main!", e);
        }
    }
}
