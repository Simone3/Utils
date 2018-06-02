package com.utils;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main extends Application {

    private final Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        try {

            logger.log(Level.INFO, "Main.start");

            // Status message
            final Text status = new Text("");

            // A text input
            Label inputLabel = new Label("Input");
            final TextField inputField = new TextField("InitialValue");

            // A button
            final Button button = new Button();
            button.setText("Button");
            button.setOnAction(event -> {

                status.setText("Processing...");

                try {

                    // Do something

                    status.setText("OK!");
                }
                catch(Exception e) {

                    status.setText(String.valueOf(e));
                    logger.log(Level.ERROR, "Uncaught exception in Main!", e);
                }
            });

			// Container
            GridPane root = new GridPane();
            root.setAlignment(Pos.CENTER);
            root.setHgap(10);
            root.setVgap(10);
            root.setPadding(new Insets(25, 25, 25, 25));

			// Add components to container
            root.add(status, 0, 0, 2, 1);
            root.add(inputLabel, 0, 1);
            root.add(inputField, 1, 1);
            root.add(button, 0, 2, 2, 1);
			
            // Setup stage
            primaryStage.setTitle("MyWindowTitle");
            primaryStage.setScene(new Scene(root, 500, 500));
            primaryStage.show();
        }
        catch(Exception e) {

            logger.log(Level.ERROR, "Uncaught exception in Main!", e);
        }
    }
}
