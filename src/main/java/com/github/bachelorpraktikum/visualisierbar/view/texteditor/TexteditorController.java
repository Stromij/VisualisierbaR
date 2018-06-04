package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.text.StyledEditorKit;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TexteditorController {

    @FXML
    private BorderPane rootPane;
    @FXML
    private Pane leftPane;
    @FXML
    private Pane topPane;
    @FXML
    private Pane centerPane;
    @FXML
    private ToggleButton copyToggle;

    private JEditorPane editorPane;
    private SwingNode editorPaneNode;
    private Stage stage;
    private File fileOfAbs;

    private Label actualLab;
    private HashMap<String ,StringBuffer> content;

    @FXML
    private void initialize() {
        // Füllle das CenterPane mit dem JEditorPane
        editorPaneNode = new SwingNode();
        editorPane = new JEditorPane()
            {public boolean getScrollableTracksViewportWidth()
                {return false ;}

             public void setSize(Dimension d)
                {if (d.width < getParent().getSize().width)
                    {d.width = getParent().getSize().width;}
                super.setSize(d);
                }
            };

        StyledEditorKit sek = new StyledEditorKit() ;
        editorPane.setEditorKit(sek);

        JScrollPane scrollPane = new JScrollPane(editorPane);

        createSwingContent(editorPaneNode, scrollPane);
        centerPane.getChildren().add(editorPaneNode);
        centerPane.setFocusTraversable(true);
        centerPane.setPadding(new Insets(5,5,5,5));
        editorPaneNode.setOnMouseClicked((event -> {editorPaneNode.requestFocus();}));


        // Fülle das TopPane mit den Funktionstasten
        SVGPath svgCopy = new SVGPath();
        svgCopy.setContent("M5 0 L12 0 L12 15 L0 15 L0 5 Z");
        copyToggle.setGraphic(svgCopy);


        content = new HashMap<>();

    }

    public void setStage(@Nonnull Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(rootPane));

        stage.centerOnScreen();

        stage.setMaximized(false);
        stage.setMaximized(true);
        stage.setTitle("Abs-Editor");
    }

    public void setPath(File file)
        {fileOfAbs = file;
         File[] allFiles = fileOfAbs.listFiles();
         GridPane grid = new GridPane();
         grid.setHgap(0);
         grid.setVgap(0);

         Label directory = new Label(file.getParentFile().toString().concat("/"));
         directory.setStyle("-fx-font-weight: bold");
         grid.add(new Label(file.getParentFile().toString().concat("/")), 0, 0);


         int rowInd = 1;
         for(File f : allFiles)
            {Label lab = new Label(f.getName());
             lab.setPadding(new Insets(5,5,5,15));
             lab.setCursor(Cursor.HAND);

             lab.setOnMouseClicked((event -> {
                    // Wechsel den Background und lade den editierten Inhalt in die HashMap
                    if(actualLab != lab)
                        {lab.setStyle("-fx-background-color: lightblue");
                         if(actualLab != null)
                             {actualLab.setStyle(null);
                              if(content.containsKey(actualLab.getText()))
                                  {content.replace(actualLab.getText(), new StringBuffer(editorPane.getText()));}
                             }
                         actualLab = lab;
                        }

                    StringBuffer buffer = new StringBuffer();


                    // Lade aus der Datei, oder falls vorhanden aus der HashMap die Daten
                    if(!content.containsKey(f.getName())) {
                        try {
                            FileInputStream fr = new FileInputStream(f);
                            InputStreamReader isr = new InputStreamReader(fr);
                            BufferedReader reader = new BufferedReader(isr);

                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                buffer.append(line + "\n");
                            }
                            reader.close();

                            content.put(f.getName(), buffer);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    else
                        {buffer = content.get(f.getName());}

                 editorPane.setText(buffer.toString());
             }));

             // Lade die Startdatei Run.abs beim Öffnen des Editors in das JEditorPane
             if(f.getName().equals("Run.abs"))
                {try {
                    FileInputStream fr = new FileInputStream(f);
                    InputStreamReader isr = new InputStreamReader(fr);
                    BufferedReader reader = new BufferedReader(isr);

                    StringBuffer buffer = new StringBuffer();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    reader.close();

                    content.put(f.getName(), buffer);

                    editorPane.setText(buffer.toString());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                 lab.setStyle("-fx-background-color: lightblue");
                 actualLab = lab;
                }

             // Füge das Label dem Grid hinzu
             grid.add(lab, 0, rowInd);
             rowInd++;
            }

         grid.setMinWidth(200);
         leftPane.getChildren().add(grid);
         leftPane.setPadding(new Insets(10,10,0,10));
        }


    private void createSwingContent(final SwingNode swingNode, final JComponent component) {
        SwingUtilities.invokeLater(() -> swingNode.setContent(component));
    }

}
