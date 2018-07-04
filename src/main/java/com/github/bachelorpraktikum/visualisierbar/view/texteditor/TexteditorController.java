package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import com.github.bachelorpraktikum.visualisierbar.abslexer.SyntaxLexer;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    private Button copyButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button undoButton;

    private JEditorPane editorPane;
    private SwingNode editorPaneNode;
    private Stage stage;
    private File fileOfAbs;

    private Label actualLab;
    private HashMap<String, StringBuffer> content;
    private ArrayList<Label> labels;
    private SyntaxLexer lex;
    private History his;
    private ChangeListener changeListener;
    private boolean firstUndo;
    private boolean firstRedo;

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
        copyButton.setGraphic(svgCopy);

        undoButton.setOnAction(ActionEvent -> undo());
        redoButton.setOnAction(ActionEvent -> redo());
        undoButton.setDisable(true);
        redoButton.setDisable(true);


        content = new HashMap<>();
        labels = new ArrayList<>();
        lex = new SyntaxLexer();
        his = new History(10);
        changeListener = new ChangeListener();
        firstUndo = true;
        firstRedo = true;
    }

    private void undo()
        {if(!his.canUndo()) {return;}
         // Wenn der das I im Changelistener größer als 0 ist muss der aktuelle Zustand
         // gespeicher werden, bevor ein erster Undo gemancht werden kann
         if(changeListener.getI() > 0 && firstUndo)
            {his.insert(new File(fileOfAbs.toString().concat("/").concat(actualLab.getText())), new StringBuffer(editorPane.getText()));
             changeListener.setI(0);
            }
         // Wenn es der erste Undo ist, dann muss der Undo doppelt gemacht werden,
         // da sonst beim Klick der aktuelle Zustand geladen wird.
         if(firstUndo)
            {firstUndo = false;
             firstRedo = true;
             changeListener.resetIsNew();
             his.undo();
            }

         // Hole das HistoryElement aus der History
         HistoryElement hisElem = his.undo();
         File fileToUndo = hisElem.getFile();
         StringBuffer doc = hisElem.getDocument();

         //ersetzte das Document in content & lexe es
         content.replace(fileToUndo.getName(), doc);

         setDocumentToPane(lex.lex(doc.toString()), 0);

         // Datei-Switch
         if(!actualLab.getText().equals(fileToUndo.getName()))
            {actualLab.setStyle(null);
                // Suche nach dem Label der neuen Datei
                for(Label l : labels)
                {if(l.getText().equals(fileToUndo.getName()))
                    {// das Label der neuen Datei ist gefunden.
                     actualLab = l;
                     actualLab.setStyle("-fx-background-color: lightblue");
                     break;
                    }
                }
            }

         // Disable/Enable die Redo & Undo-Button
         undoButton.setDisable(!his.canUndo());
         redoButton.setDisable(!his.canRedo());
        }

    private void redo()
        {if(!his.canRedo()) {return;}
         // beim ersten Redo muss wird der aktuelle Zustand geladen, sodass ein doppeltes Redo
         // stattfinden muss
         if(firstRedo)
            {firstRedo = false;
             firstUndo = true;
             changeListener.resetIsNew();
             his.redo();
            }

         // Hole das HistoryElement aus der History
         HistoryElement hisElem = his.redo();
         File fileToRedo = hisElem.getFile();
         StringBuffer doc = hisElem.getDocument();

         //ersetzte das Document in content

         content.replace(fileToRedo.getName(), doc);

         setDocumentToPane(lex.lex(doc.toString()), 0);

         // Datei-Switch
         if(!actualLab.getText().equals(fileToRedo.getName()))
            {actualLab.setStyle(null);
             // Suche nach dem Label der neuen Datei
             for(Label l : labels)
                {if(l.getText().equals(fileToRedo.getName()))
                    {// das Label der neuen Datei ist gefunden.
                     actualLab = l;
                     actualLab.setStyle("-fx-background-color: lightblue");
                     break;
                    }
                }
            }

         // Disable/Enable die Redo & Undo-Button
         undoButton.setDisable(!his.canUndo());
         redoButton.setDisable(!his.canRedo());
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
                                  {if(!content.get(actualLab.getText()).equals(editorPane.getText()))
                                      {his.insert(new File(fileOfAbs.toString().concat(actualLab.getText())), new StringBuffer(editorPane.getText()));}
                                   content.replace(actualLab.getText(), new StringBuffer(editorPane.getText()));
                                  }
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

                 Document doc = lex.lex(buffer.toString());

                 // Einstellen der Tab-Size
                 TabSizeEditorKit tab = new TabSizeEditorKit();
                 tab.setTabSize(editorPane.getFontMetrics(editorPane.getFont()).charWidth('M'));
                 editorPane.setEditorKit(tab);

                 editorPane.setDocument(doc);
                 addGridBackground(doc);
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

                 Document result = lex.lex(editorPane.getText());

                 setDocumentToPane(result, 0);

                 his.insert(new File(fileOfAbs.toString().concat("/").concat(actualLab.getText())), new StringBuffer(editorPane.getText()));
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
        private int i = 0;
        private DocumentEvent lastEvent;
        private boolean isNew = true;

        public void insertUpdate(DocumentEvent e) {
            if(lastEvent != null && !lastEvent.equals(e))
                {safe(); lastEvent = e;}
            else if(i > 10 || i + e.getLength() > 10)
                {safe(); i = 0;}
            else if(isNew)
                {isNew = false;
                 safe(); i = 0;
                }
            else
                {i += e.getLength();}
            update();System.out.println(e.getLength());

        }

        public void removeUpdate(DocumentEvent e)
            {if(lastEvent != null && !lastEvent.equals(e))
                {safe(); lastEvent = e;}
             else if(i > 10 || i + e.getLength() > 10)
                {safe(); i = 0;}
             else
                {i += e.getLength();}
             update(); System.out.println(e.getLength());

            }

        public void changedUpdate(DocumentEvent e) {

        }

        private void safe()
            {his.insert(new File(fileOfAbs.toString().concat("/").concat(actualLab.getText())), new StringBuffer(editorPane.getText()));
             undoButton.setDisable(!his.canUndo());
             redoButton.setDisable(!his.canRedo());
            }

        public void resetIsNew()
            {isNew = true;}

        public int getI()
            {return i;}

        public void setI(int newi)
            {this.i = newi;}

        private void update(){
            int position = editorPane.getCaretPosition();
            SyntaxLexer lex = new SyntaxLexer();
            Document result = lex.lex(editorPane.getText());

            setDocumentToPane(result, position);

            firstRedo = true;
            firstUndo = true;
        }
    }


    /**
     * adds a background highlight to every grid
     * @param doc the document to highlight
     */
    private void addGridBackground(Document doc)
        {editorPane.getHighlighter().removeAllHighlights();

         String content = editorPane.getText().toLowerCase();
         if(content.contains("grid end") && content.contains("grid start"))
            {int offset = 0;
             int startInt;
             int endInt;

             GridHighlighter highlighter = new GridHighlighter();
             while((startInt = content.indexOf("grid start", offset)) != -1)
                {if(content.indexOf("grid start", startInt+ 1) < content.indexOf("grid end", startInt + 1) &&
                    content.indexOf("grid start", startInt + 1) != -1    ||
                    content.indexOf("grid end", startInt + 1) == -1)
                    {// Falls es zu dem gefundenen start kein end gibt
                     offset = startInt + 1;
                     continue;
                    }
                 offset = startInt + 1;

                 // da die Makierung bis zum Ende der Zeile gehen soll, in der grid end steht,
                 // suche erst die Position von grid end und dann die Position vom nächsten
                 // Zeilenumbruch und addiere 1.
                 endInt = content.indexOf("\n", content.indexOf("grid end", offset)) + 1;

                 startInt = content.lastIndexOf("\n", startInt) + 1;


                 try
                    {editorPane.getHighlighter().addHighlight(startInt, endInt, highlighter); }
                 catch (BadLocationException e)
                    {e.printStackTrace();}

                }

            }
        }


    /**
     * Sets a given Document to the editorpane with syntax highlighting, tabs
     * and grid-highlightinh
     * @param doc the document to set
     * @param pos the cursor position
     */

    private void setDocumentToPane(Document doc, int pos)
        {// Tabstop
         TabSizeEditorKit tab = new TabSizeEditorKit();
         tab.setTabSize(editorPane.getFontMetrics(editorPane.getFont()).charWidth('M'));
         editorPane.setEditorKit(tab);


         editorPane.setDocument(doc);
         addGridBackground(doc);
         editorPane.setCaretPosition(pos);
         editorPane.getDocument().addDocumentListener(changeListener);
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
