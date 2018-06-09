package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import com.github.bachelorpraktikum.visualisierbar.abslexer.SyntaxLexer;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

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
    private HashMap<String, StringBuffer> content;
    private ArrayList<Label> labels;

    @FXML
    private void initialize() {
        // Füllle das CenterPane mit dem JEditorPane
        editorPaneNode = new SwingNode();
        editorPane = new JEditorPane()
            {@Override
             public boolean getScrollableTracksViewportWidth()
                {return false ;}

             @Override
             public void setSize(Dimension d)
                {if (d.width < getParent().getSize().width)
                    {d.width = getParent().getSize().width;}
                super.setSize(d);
                }
            };

        StyledEditorKit sek = new StyledEditorKit() ;
        editorPane.setEditorKit(sek);

        JScrollPane scrollPane = new JScrollPane(editorPane);

        TextLineNumber tln = new TextLineNumber(editorPane);
        scrollPane.setRowHeaderView(tln);

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
        labels = new ArrayList<>();

    }


    /**
     * The {@link #rootPane} will be displayed on the given stage.
     *
     * @param stage Stage on which the scene will be displayed
     */
    public void setStage(@Nonnull Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(rootPane));

        stage.centerOnScreen();

        stage.setMaximized(false);
        stage.setMaximized(true);
        stage.setTitle("Abs-Editor");
    }


    /**
     * Sets the path to the ABS-Directory, displays its content as labels at the left hand side of the editor
     * and adds Listeners to the labels to switch the content of the EditorPane and the style of the choosen
     * input-File
     *
     * @param file to the ABS-Directory
     */
    public void setPath(File file)
        {fileOfAbs = file;
         File[] allFiles = fileOfAbs.listFiles();
         GridPane grid = new GridPane();
         grid.setHgap(0);
         grid.setVgap(0);

         Label directory = new Label(file.toString().concat("/"));
         directory.setStyle("-fx-font-weight: bold");
         grid.add(directory, 0, 0);


         int rowInd = 1;
         for(File f : allFiles)
            {Label lab = new Label(f.getName());
             lab.setPadding(new Insets(5,5,5,15));
             lab.setCursor(Cursor.HAND);

             // Listener für Maus-Klicks: Der user möchte eine andere Datei sehen
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

                 SyntaxLexer lex = new SyntaxLexer();
                 Document result = lex.lex(editorPane.getText());
                 editorPane.setDocument(result);

                 editorPane.getDocument().addDocumentListener(new ChangeListener());
                }

             // Füge das Label dem Grid hinzu
             grid.add(lab, 0, rowInd);
             labels.add(lab);
             rowInd++;
            }

         grid.setMinWidth(200);
         leftPane.getChildren().add(grid);
         leftPane.setPadding(new Insets(10,10,0,10));
        }


    /**
     * Inlineclass for the DocumentListener to adapt the Syntaxhighlighting
     */
    class ChangeListener implements DocumentListener {


        public void insertUpdate(DocumentEvent e) {
            update();
        }

        public void removeUpdate(DocumentEvent e)
            {update();}

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }

        private void update(){
            int position = editorPane.getCaretPosition();
            SyntaxLexer lex = new SyntaxLexer();
            Document result = lex.lex(editorPane.getText());
            editorPane.setDocument(result);
            editorPane.setCaretPosition(position);
            editorPane.getDocument().addDocumentListener(new ChangeListener());
        }
    }


    /**
     * Creates SwingContent of an given JCompnent
     * @param swingNode
     * @param component
     */
    private void createSwingContent(final SwingNode swingNode, final JComponent component) {
        SwingUtilities.invokeLater(() -> swingNode.setContent(component));
    }

}
