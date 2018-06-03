package com.utils;

import com.utils.common.graphic.SimpleGrid;
import com.utils.common.graphic.SimpleTextualTable;
import com.utils.renamefiles.RenameFilesFromList;
import com.utils.renamefiles.RenameResult;
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

            RenameFilesFromList renamer = new RenameFilesFromList();

            // Status message
            final Text status = new Text("");

            // Input for folder
            Label folderLabel = new Label("Folder with the files to rename");
            final TextField folderInput = new TextField(System.getProperty("user.home") + "\\Desktop\\files");

            // Input for names
            Label namesLabel = new Label("Text file with new file names");
            final TextField namesInput = new TextField(System.getProperty("user.home") + "\\Desktop\\names.txt");

            // Preview table
            final SimpleTextualTable<RenameResult> table = new SimpleTextualTable<>();
            table.addSimpleTextColumn("From", "fromFile");
            table.addSimpleTextColumn("To", "toFile");
            table.addSimpleTextColumn("Notes", "message");

            // Confirm button
            final Button confirmButton = new Button();
            confirmButton.setText("Confirm");
            confirmButton.setDisable(true);
            confirmButton.setOnAction(event -> {

                try {

                    renamer.process(namesInput.getText(), folderInput.getText(), false);

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

                    List<RenameResult> preview = renamer.process(namesInput.getText(), folderInput.getText(), true);

                    boolean canRename = true;
                    for(RenameResult rename: preview) {

                        if(!rename.isValid()) {

                            canRename = false;
                        }
                    }

                    table.getData().clear();
                    table.getData().addAll(preview);

                    confirmButton.setDisable(!canRename);

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
            root.addNextSimpleRow(namesLabel, namesInput);
            root.addNextSimpleRow(previewButton, confirmButton);
            root.addNextSimpleRow(table);

            // Setup stage
            primaryStage.setTitle("Rename Files");
            primaryStage.setScene(new Scene(root, 500, 500));
            primaryStage.show();
        }
        catch(Exception e) {

            logger.log(Level.ERROR, "Uncaught exception in Main!", e);
        }
    }
}
