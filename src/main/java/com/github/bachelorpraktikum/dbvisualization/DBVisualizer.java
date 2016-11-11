package com.github.bachelorpraktikum.dbvisualization;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class DBVisualizer extends Application {
    static {
        try (InputStream in = DBVisualizer.class.getResourceAsStream("/logging.properties")) {
            LogManager.getLogManager().readConfiguration(in);
        } catch (IOException e) {
            System.out.println("Can't initialize logging!");
            System.exit(100);
        }
    }

    private static final Logger log = Logger.getLogger(DBVisualizer.class.getName());

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(DBVisualizer.class, args);
    }
}
