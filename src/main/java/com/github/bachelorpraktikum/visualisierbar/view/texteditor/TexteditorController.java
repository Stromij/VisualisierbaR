package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import javax.swing.*;

public class TexteditorController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private Pane leftPane;
    @FXML
    private Pane topPane;
    @FXML
    private Pane centerPane;

    private JEditorPane editorPane;
    private SwingNode editorPaneNode;
    private Stage stage;

    @FXML
    private void initialize() {
         // Generiere das GUI
         editorPaneNode = new SwingNode();
         editorPane = new JEditorPane();

         createSwingContent(editorPaneNode, editorPane);
         centerPane.getChildren().add(editorPaneNode);
    }

    public void setStage(@Nonnull Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(rootPane));

        stage.centerOnScreen();

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        stage.setMaximized(false);
        stage.setMaximized(true);
    }


    private void createSwingContent(final SwingNode swingNode, final JComponent component) {
        SwingUtilities.invokeLater(() -> swingNode.setContent(component));
    }

}
