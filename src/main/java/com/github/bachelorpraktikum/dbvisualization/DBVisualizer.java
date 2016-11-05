package com.github.bachelorpraktikum.dbvisualization;

import javafx.application.Application;
import javafx.stage.Stage;

public class DBVisualizer extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(DBVisualizer.class, args);
    }
}
