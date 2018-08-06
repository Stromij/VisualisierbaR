package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import com.github.bachelorpraktikum.visualisierbar.abslexer.SyntaxLexer;
import com.github.bachelorpraktikum.visualisierbar.view.MainController;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer.PDFData;
import com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer.PDFViewer;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.util.*;
import javafx.scene.paint.Paint;

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
    private Button saveButton;
    @FXML
    private Button playButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button undoButton;
    @FXML
    private TextField searchText;

    private JEditorPane editorPane;
    private SwingNode editorPaneNode;
    private Stage stage;
    private Stage mainStage;
    private File fileOfAbs;

    private Label actualLab;
    private HashMap<String, StringBuffer> content;
    private ArrayList<Label> labels;
    private SyntaxLexer lex;
    private History his;
    private ChangeListener changeListener;
    private boolean firstUndo;
    private boolean firstRedo;
    private PDFViewer pdfViewer;

    private MainController parent;

    @FXML
    private void initialize() {
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
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
        editorPaneNode.setOnMouseClicked((event -> editorPaneNode.requestFocus()));


        // Fülle das TopPane mit den Funktionstasten und initialisiere sie
        SVGPath svgSave = new SVGPath();
        svgSave.setContent("M5 0 L12 0 L12 15 L0 15 L0 5 Z");
        saveButton.setGraphic(svgSave);

        saveButton.setOnAction(ActionEvent -> save(true));

        SVGPath svgPlay = new SVGPath();
        svgPlay.setContent("M0 0 L12 7 L0 14 L0 0 Z");
        playButton.setGraphic(svgPlay);

        playButton.setOnAction(ActionEvent -> play());

        undoButton.setOnAction(ActionEvent -> undo());
        redoButton.setOnAction(ActionEvent -> redo());
        undoButton.setDisable(true);
        redoButton.setDisable(true);

        // Konfiguriere das Textfeld für die Suche
        searchText.setPromptText(bundle.getString("search"));
        searchText.setBackground(new Background(new BackgroundFill(Paint.valueOf("FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));

        searchText.addEventHandler(KeyEvent.KEY_PRESSED, event ->{
            searchText.setBackground(new Background(new BackgroundFill(Paint.valueOf("FFFFFF"), CornerRadii.EMPTY, Insets.EMPTY)));
            if(event.getCode() == KeyCode.ENTER)
                {if(searchText.getText().length() > 0)
                    {startSearch(searchText.getText(), editorPane.getCaretPosition());}
                }
        });

        //Konfiguriere das JEditorPane für Spezifikationen
        editorPane.addMouseListener(new MouseListener() {
                                        @Override
                                        public void mouseClicked(MouseEvent e) {

                                        }

                                        @Override
                                        public void mousePressed(MouseEvent event) {
                                            if(event.getButton() == MouseEvent.BUTTON3)
                                                {int offset = editorPane.viewToModel( event.getPoint() );
                                                 try{
                                                     int rowStart = Utilities.getRowStart(editorPane, offset);
                                                     int rowEnd = Utilities.getRowEnd(editorPane, offset);
                                                     String row = editorPane.getText(rowStart, rowEnd - rowStart);
                                                     if(row.matches(".*//\\[[^:\\\"\\]]*:[^:\\\"\\]]*\\]\\s*"))
                                                        {int specStart = row.indexOf("[");
                                                         int specEnd = row.indexOf("]");
                                                         String model = row.substring(specStart+1, row.indexOf(":"));
                                                         String attri = row.substring(row.indexOf(":")+1, specEnd);
                                                         if(rowStart + specStart > offset || rowStart + specEnd < offset)
                                                            {// Klick war zwar in der Zeile, aber nicht auf dem Spec-Element
                                                             return;
                                                            }
                                                         PDFData pdfData = new PDFData(model, attri);
                                                         pdfViewer = new PDFViewer(pdfData.getLocation());
                                                         pdfViewer.setPage(pdfData.getStartPage());
                                                         pdfViewer.highlight(pdfData.getStartPage(),pdfData.getEndPage(),pdfData.getStartY(), pdfData.getEndY());
                                                        }
                                                 }
                                                 catch(BadLocationException | IOException e){
                                                     e.printStackTrace();
                                                 }

                                                }
                                        }

                                        @Override
                                        public void mouseReleased(MouseEvent e) {

                                        }

                                        @Override
                                        public void mouseEntered(MouseEvent e) {

                                        }

                                        @Override
                                        public void mouseExited(MouseEvent e) {

                                        }
                                    }

        );

        // Füge Shortcuts hinzu
        rootPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.Z && event.isControlDown()) { // Strg + Z
               undoButton.fire();
            }
            if (event.getCode() == KeyCode.Y && event.isControlDown()) { // Strg + Y
                redoButton.fire();
            }
            if (event.getCode() == KeyCode.S && event.isControlDown()) { // Strg + S
                saveButton.fire();
            }
            if (event.getCode() == KeyCode.R && event.isControlDown()) { // Strg + R
                playButton.fire();
            }
            if (event.getCode() == KeyCode.L && event.isControlDown()) {  // Strg + L
                goToLine();
            }
            if (event.getCode() == KeyCode.F && event.isControlDown()) {  // Strg + F
                searchText.requestFocus();
            }
        });

        // Füge Tooltips hinzu
        TooltipUtil.install(undoButton, new Tooltip(bundle.getString("undo") +  "(" + KeyCode.CONTROL.getName() + " + " + KeyCode.Z.getName() + ")"));
        TooltipUtil.install(redoButton, new Tooltip(bundle.getString("redo") +  "(" + KeyCode.CONTROL.getName() + " + " + KeyCode.Y.getName() + ")"));
        TooltipUtil.install(saveButton, new Tooltip(bundle.getString("save") +  "(" + KeyCode.CONTROL.getName() + " + " + KeyCode.S.getName() + ")"));
        TooltipUtil.install(playButton, new Tooltip(bundle.getString("recompile") +  "(" + KeyCode.CONTROL.getName() + " + " + KeyCode.R.getName() + ")"));
        TooltipUtil.install(searchText, new Tooltip(bundle.getString("search")));



        // Initialisiere sonstige Variablen
        content = new HashMap<>();
        labels = new ArrayList<>();
        lex = new SyntaxLexer();
        his = new History(10);
        changeListener = new ChangeListener();
        firstUndo = true;
        firstRedo = true;
    }


    private void startSearch(String textToSearch, int offset)
        {setDocumentToPane(editorPane.getDocument(), editorPane.getCaretPosition());

         GridHighlighter yellowHighlighter = new GridHighlighter(new Color(255, 243, 79));
         GridHighlighter orangeHighlighter = new GridHighlighter(new Color(255, 194, 110));


         String text = editorPane.getText();
         if(!text.contains(textToSearch))                           // der gesuchte Text konnte nicht gefunden werden
            {searchText.setBackground(new Background(new BackgroundFill(Paint.valueOf("FFDBC8"), CornerRadii.EMPTY, Insets.EMPTY)));
             return;
            }


         int startYellow;
         int endYellow = 0;

         int firstYellow = -1;
         int firstEndYellow = -1;
         boolean cursorPosition = false;            // Wurde der Cursor schon neu positioniert?

         // Suche und Markiere
         while(text.indexOf(textToSearch,endYellow + 1) != -1)
            {startYellow = text.indexOf(textToSearch,endYellow + 1);
             endYellow = startYellow + textToSearch.length();

             if(firstYellow == -1)
                {firstYellow = startYellow;
                 firstEndYellow = endYellow;
                }

             try {// Positioniere Cursor neu, falls es das erste :
                 if (!cursorPosition && startYellow >= offset) {
                     editorPane.setCaretPosition(startYellow + 1);
                     cursorPosition = true;
                     editorPane.getHighlighter().addHighlight(startYellow, endYellow, orangeHighlighter);
                 } else {
                    editorPane.getHighlighter().addHighlight(startYellow, endYellow, yellowHighlighter);
                 }
             }
             catch (BadLocationException e)
                {e.printStackTrace();}
            }

         if(!cursorPosition)
            {editorPane.setCaretPosition(firstYellow + 1);
             try
                {editorPane.getHighlighter().addHighlight(firstYellow, firstEndYellow, orangeHighlighter);}
             catch (BadLocationException e)
                {e.printStackTrace();}
            }
        }


    /**
     * Opens a dialog, asks the User for a line number and jumps to the given line
     */
    private void goToLine()
        {int number;
         try{number = goToLineDialog();
             if(number == -1) {return;}                 // Dialog wurde abgebrochen
             int i = 0;
             for(int n = 0; n < number - 1; n++)
                {try
                    {i = Utilities.getRowEnd(editorPane,i) + 1;}
                 catch (BadLocationException e)
                    {i = Utilities.getRowStart(editorPane,editorPane.getDocument().getLength());break;}
                }
              editorPane.setCaretPosition(i);
             }
         catch(NumberFormatException e) {               // Es wurde keine Nummer eingegeben
             ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
             Alert alert = new Alert(Alert.AlertType.ERROR);
             String headerText = bundle.getString("number_io_exception_header");
             alert.setHeaderText(headerText);
             String contentText = bundle.getString("number_io_exception_content");
             alert.setContentText(contentText);
             alert.showAndWait();
             }
         catch(BadLocationException e){
             // Shold not happen
         }

         editorPaneNode.requestFocus();
        }

    /**
     * Asks the Users for a line nubmer to got to
     * @return returns the Number given by the User, -2 if he press Cancel
     * @throws NumberFormatException if the user does not enter a positiv Integer
     */
    private int goToLineDialog() throws NumberFormatException
        {ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
         TextInputDialog dialog = new TextInputDialog();
         dialog.setTitle(bundle.getString("go_to_line_header"));
         dialog.setGraphic(null);
         dialog.setHeaderText(bundle.getString("go_to_line_content"));
         Optional<String> s = dialog.showAndWait();
         if (s.isPresent())
            {if(s.get().length() == 0)
                {return 1;}
             if(Integer.parseInt(s.get()) >= 0)
                return Integer.parseInt(s.get());
            }
         else
            {return -1;}
         throw new NumberFormatException();
        }

    /**
     * Recompile the edited ABS-Files and reopen the Mainview without the editor!
     */
    private void play()
        {save(false);
         parent.play(false);
        }

    public Stage getStage()
        {return stage;}


    /**
     * Save the Content of the EditorPane to the given Path (fileOfAbs)
     * @param errorReporting if true all Errors will be shown in a Dialog, otherwise not
     */
    private void save(boolean errorReporting)
        {// Lade den aktuellen Zustand der editorpane in die Content-Map
         content.replace(actualLab.getText(), new StringBuffer(editorPane.getText()));

         // Gehe jeden Eintrag in der Content-Map durch, da nur diese geändert worden sein könnten
         for(Map.Entry<String, StringBuffer> ent : content.entrySet())
            {String pathname = fileOfAbs.toString().concat("/").concat(ent.getKey());
             String file = ent.getValue().toString();

             // Schreibe in die Dateien
             try {
                 FileWriter fw = new FileWriter(pathname);
                 BufferedWriter bw = new BufferedWriter(fw);

                 bw.write(file);

                 bw.close();
                 fw.close();

                 if(errorReporting) {
                     ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
                     Alert alert = new Alert(Alert.AlertType.INFORMATION);
                     String headerText = bundle.getString("file_saved_header");
                     alert.setHeaderText(headerText);
                     String contentText = bundle.getString("file_saved_content");
                     alert.setContentText(contentText);
                     alert.initOwner(stage);
                     alert.showAndWait();
                 }

                }
             catch(IOException e) {
                 if(errorReporting) {
                     ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
                     Alert alert = new Alert(Alert.AlertType.ERROR);
                     String headerText = bundle.getString("file_not_save_header");
                     alert.setHeaderText(headerText);
                     String contentText = String.format(bundle.getString("file_not_save_content"), pathname);
                     contentText = String.format(contentText, e.getMessage());
                     alert.setContentText(contentText);
                     alert.showAndWait();
                    }
                  }
            }
        }

    private void undo()
        {if(!his.canUndo()) {return;}
         // Wenn der das I im Changelistener größer als 0 ist muss der aktuelle Zustand
         // gespeicher werden, bevor ein erster Undo gemancht werden kann
         if(changeListener.getI() > 0 && firstUndo)
            {his.insert(new File(fileOfAbs.toString().concat("/").concat(actualLab.getText())), new StringBuffer(editorPane.getText()), editorPane.getCaretPosition());
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
         int cursorPosition = hisElem.getCursorPosition();

         //ersetzte das Document in content & lexe es
         content.replace(fileToUndo.getName(), doc);

         setDocumentToPane(lex.lex(doc.toString()), cursorPosition);

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
         int cursorPosition = hisElem.getCursorPosition();

         //ersetzte das Document in content

         content.replace(fileToRedo.getName(), doc);

         setDocumentToPane(lex.lex(doc.toString()), cursorPosition);

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
     * @param editorStage Stage on which the texteditor will be displayed
     * @param mainStage the Stage of the MainView
     */
    public void setStage(@Nonnull Stage editorStage, @Nonnull Stage mainStage) {
        this.mainStage = mainStage;
        this.stage = editorStage;
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
                                      {his.insert(new File(fileOfAbs.toString().concat(actualLab.getText())), new StringBuffer(editorPane.getText()), editorPane.getCaretPosition());}
                                   content.replace(actualLab.getText(), new StringBuffer(editorPane.getText()));
                                  }
                             }
                         actualLab = lab;
                        }

                    StringBuffer buffer = new StringBuffer();


                    // Lade aus der Datei, oder falls vorhanden aus der HashMap die Daten
                    if(!content.containsKey(f.getName())) {
                        buffer = loadFile(f);
                    }

                    else
                        {buffer = content.get(f.getName());}

                 setDocumentToPane(lex.lex(buffer.toString()), 0);
             }));

             // Lade die Startdatei Run.abs beim Öffnen des Editors in das JEditorPane
             if(f.getName().equals("Run.abs"))
                {StringBuffer buf = loadFile(f);

                 lab.setStyle("-fx-background-color: lightblue");
                 actualLab = lab;

                 setDocumentToPane(lex.lex(buf.toString()), 0);

                 his.insert(new File(fileOfAbs.toString().concat("/").concat(actualLab.getText())), new StringBuffer(editorPane.getText()), editorPane.getCaretPosition());
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
     * lädt einen File in die Content-Map
     * @param f
     */
    private StringBuffer loadFile(File f)
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

            return buffer;
         } catch (IOException e) {
            e.printStackTrace();
         }
         return null;
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
            update();

        }

        public void removeUpdate(DocumentEvent e)
            {if(lastEvent != null && !lastEvent.equals(e))
                {safe(); lastEvent = e;}
             else if(i > 10 || i + e.getLength() > 10)
                {safe(); i = 0;}
             else
                {i += e.getLength();}
             update();

            }

        public void changedUpdate(DocumentEvent e) {

        }

        private void safe()
            {his.insert(new File(fileOfAbs.toString().concat("/").concat(actualLab.getText())), new StringBuffer(editorPane.getText()), editorPane.getCaretPosition());
             undoButton.setDisable(!his.canUndo());
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

            setDocumentToPane(lex.lex(editorPane.getText()), position);

            firstRedo = true;
            firstUndo = true;

            save(false);
        }
    }


    /**
     * adds a background highlight to every grid
     * The background is gray if a start and an end exists
     * The background is red if there is only a start or an end
     */
    private void addGridBackground()
        {editorPane.getHighlighter().removeAllHighlights();

         String content = editorPane.getText().toLowerCase();
         if(content.contains("grid end") || content.contains("grid start"))
            {int offset = 0;
             int startInt;
             int endInt;
             int startRed;
             int endRed;

             GridHighlighter grayHighlighter = new GridHighlighter(new Color(234, 234, 234));
             GridHighlighter redHighlighter = new GridHighlighter(new Color(255, 219, 200));

             // Suche nach end vor dem ersten start
             while(content.indexOf("grid end", offset) < content.indexOf("grid start", offset) && content.indexOf("grid end", offset)  != -1||
                   content.indexOf("grid start", offset) == -1 && content.indexOf("grid end", offset) != -1
                  )
                {offset = content.indexOf("grid end", offset);
                 offset =  content.indexOf("grid end", offset) + 1;
                 startRed = content.lastIndexOf("\n", offset) + 1;
                 endRed = content.indexOf("\n", offset) + 1;
                 endRed = endRed == 0 ? content.length() : endRed;


                 try
                    {editorPane.getHighlighter().addHighlight(startRed, endRed, redHighlighter); }
                 catch (BadLocationException e)
                    {e.printStackTrace();}
                }


             while((startInt = content.indexOf("grid start", offset)) != -1)
                {if(content.indexOf("grid start", startInt+ 1) < content.indexOf("grid end", startInt + 1) &&
                    content.indexOf("grid start", startInt + 1) != -1    ||
                    content.indexOf("grid end", startInt + 1) == -1)
                    {// Falls es zu dem gefundenen start kein end gibt
                     startRed = content.lastIndexOf("\n", startInt) + 1;
                     endRed = content.indexOf("\n", startInt) + 1;
                     endRed = endRed == 0 ? content.length() : endRed;
                     offset = endRed;
                     try
                        {editorPane.getHighlighter().addHighlight(startRed, endRed, redHighlighter); }
                     catch (BadLocationException e)
                        {e.printStackTrace();}
                     continue;
                    }
                 offset = startInt + 1;

                 // da die Makierung bis zum Ende der Zeile gehen soll, in der grid end steht,
                 // suche erst die Position von grid end und dann die Position vom nächsten
                 // Zeilenumbruch und addiere 1.
                 endInt = content.indexOf("\n", content.indexOf("grid end", offset)) + 1;
                 endInt = endInt >= content.length() || endInt < startInt ? content.length() : endInt;



                 startInt = content.lastIndexOf("\n", startInt) + 1;

                 offset = endInt;
                 try
                    {editorPane.getHighlighter().addHighlight(startInt, endInt, grayHighlighter); }
                 catch (BadLocationException e)
                    {e.printStackTrace();}

                 // Suche abschließend noch nach end-Kommentaren zwischen jetzt und dem nächsten start/EOF

                 while(content.indexOf("grid start", offset) > content.indexOf("grid end", offset) && content.indexOf("grid end", offset) != -1 ||
                       content.indexOf("grid start", offset) == -1 && content.indexOf("grid end", offset) != -1
                      )
                    {offset =  content.indexOf("grid end", offset) + 1;
                     startRed = content.lastIndexOf("\n", offset) + 1;
                     endRed = content.indexOf("\n", offset) + 1;
                     endRed = endRed == 0 ? content.length() : endRed;

                     try
                        {editorPane.getHighlighter().addHighlight(startRed, endRed, redHighlighter); }
                     catch (BadLocationException e)
                        {e.printStackTrace();}

                    }
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
         addGridBackground();
         pos = pos >= editorPane.getText().length() - 1 ? editorPane.getText().length() : pos;
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

    /**
     * Reloads all files to the content-Map by destroying the old Map and displays
     * the reloaded files.
     */
    public void reloadAll()
        {content.clear();
         StringBuffer buf = loadFile(new File(fileOfAbs.toString().concat("/").concat(actualLab.getText())));
         setDocumentToPane(lex.lex(buf.toString()), 0);
        }


    public void setParent(MainController parent)
        {this.parent = parent;}

}
