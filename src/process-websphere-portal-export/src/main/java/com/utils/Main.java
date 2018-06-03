package com.utils;

import com.utils.common.graphic.SimpleGrid;
import com.utils.common.xml.XmlParserException;
import com.utils.portal.PortalPageExportParser;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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

	    	// "Input" input
	    	Label inputLabel = new Label("Input XML");
	    	final TextField inputField = new TextField(System.getProperty("user.home") + "\\Desktop\\in.xml");

	    	// "Output" input
	    	Label outputLabel = new Label("Output XML");
	    	final TextField outputField = new TextField(System.getProperty("user.home") + "\\Desktop\\out.xml");

	        // Process button
	        final Button confirmButton = new Button();
	        confirmButton.setText("Process");
	        confirmButton.setOnAction(event -> {

				status.setText("Parsing...");

				try {

					new PortalPageExportParser().fixPageExportXml(inputField.getText(), outputField.getText());

					status.setText("OK!");
				}
				catch(XmlParserException e) {

					status.setText("Error: " + e);
					logger.log(Level.ERROR, e);
				}
				catch(Exception e) {

					logger.log(Level.ERROR, "Uncaught exception in Main!", e);
				}
			});

			// Container
			SimpleGrid root = new SimpleGrid(2);

			// Add components to container
			root.addNextSimpleRow(status);
			root.addNextSimpleRow(inputLabel, inputField);
			root.addNextSimpleRow(outputLabel, outputField);
			root.addNextSimpleRow(confirmButton);

	        // Setup stage
	        primaryStage.setTitle("Process Portal Page");
	        primaryStage.setScene(new Scene(root, 500, 500));
	        primaryStage.show();
    	}
    	catch(Exception e) {

    		logger.log(Level.ERROR, "Uncaught exception in Main!", e);
    	}
    }
}
