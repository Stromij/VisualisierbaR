package com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PDFSelectionWindow {

    private static final String PDF_DIRECTORY = "src/main/csv/github/bachelorpraktikum/visualisierbar/guidelines/";          // TODO in config-Datei auslagern?

    private ObservableList<PDFSelectionLines> dataLines;

    public PDFSelectionWindow() {
        dataLines = FXCollections.observableArrayList();

        // Lade alle vorhandenen PDFs in dataLines
        File PDFFile = new File("src/main/csv/github/bachelorpraktikum/visualisierbar/guidelines/");
        String[] files = PDFFile.list();

        // Sortiere das Array mit den PDF-Dateien
        Arrays.sort(files);             // TODO sorting Algorithm? (Groß- und Kleinschreibung wird (falsch?) beachtet)

        // Filtere alle PDF-Dateien raus und verwerfe die anderen.
        for (String f: files) {
            if(f.endsWith(".pdf"))
                {dataLines.add(new PDFSelectionLines(f));}
        }

    }





    /**
     * Opens a Dialog for choosing a PDF-File
     */
    public void display(){
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");



        // Label und Suchfeldgenerierung
        Label label = new Label(bundle.getString("found_pdf") + ":");

        TextField textField = new TextField();
        textField.setPromptText(bundle.getString("search"));



        // Layout für die Head-Zeile (Suche und Label)
        GridPane pane = new GridPane();
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        pane.getColumnConstraints().addAll(col1,col2);

        pane.setHgap(2);
        pane.setVgap(1);

        pane.setAlignment(Pos.CENTER);


        pane.add(label, 0,0);
        pane.add(textField, 1,0);



        // Generiere die leere Übersichtstabelle
        TableView table = new TableView();

        // Passe das Layout ein wenig an, damit auch im Unfokussierten TableView die Zeile Blau selektiert werden kann
        table.setStyle("-fx-selection-bar: lightblue; -fx-selection-bar-non-focused: lightblue;");


        // Füge die Tabellenspalten ein
        TableColumn nameColumn = new TableColumn(bundle.getString("guideline"));
        nameColumn.setCellValueFactory(
                new PropertyValueFactory<PDFSelectionLines,String>("name")
        );
        nameColumn.setMinWidth(600);



        // Füge jeder Zeile einen Listener hinzu, um einen Doppelklick abzufangen
        table.setRowFactory(tv -> {
            TableRow<PDFSelectionLines> tableRow = new TableRow<>();
            tableRow.setOnMouseClicked(e -> {
                if (! tableRow.isEmpty() && e.getButton()== MouseButton.PRIMARY
                        && e.getClickCount() == 2) {
                    openPDFViewer(new File(PDF_DIRECTORY.concat(tableRow.getItem().getName())));
                }
            });
            return tableRow ;
        });


        // Fülle die Zellen mit den Daten
        table.setItems(this.dataLines);
        table.getColumns().addAll(nameColumn);

        // Formatiere die Übersichtstabelle
        table.setFixedCellSize(25);
        table.setMaxHeight(350);
        table.setMinWidth(600);
        table.setPrefHeight(table.getItems().size() * 25 + 28);


        // Wrappe alles
        VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(20, 10, 0, 10));
        vbox.getChildren().addAll(pane, table);


        // Nur für Dinge, die bei einem gedrückten Key sich wieder holen sollen!
        textField.addEventHandler(KeyEvent.ANY, event -> {
            // Fange die Up- and Down-Keys um die Selection hoch oder runter zu bewegen. Ziehe dabei, falls nötg, den
            // Scroll nach.
            int actualSelectedRow = table.getSelectionModel().getSelectedIndex();
            if(event.getCode() == KeyCode.DOWN && actualSelectedRow + 1 < table.getItems().size()){
                table.requestFocus();
                table.getSelectionModel().clearAndSelect(actualSelectedRow + 1);
            }
            else if(event.getCode() == KeyCode.UP  && actualSelectedRow - 1 >= 0) {
                table.requestFocus();
                table.getSelectionModel().clearAndSelect(actualSelectedRow - 1);
            }

            // Sollte das Suchtextfeld textField nicht leer sein und das obige nicht zutreffen, selectiere standardmäßig
            // den obersten Eintrag
            else if(textField.getText().length() > 0 && actualSelectedRow == -1) {
                table.getSelectionModel().clearAndSelect(0);
                table.scrollTo(0);
            }
        });


        // Nur für Dinge, die beim Loslassen eines Keys getan werden sollen!
        // Füge dem Fenster einen Listener für beliebigen Key hinzu, um Schnellsuche zu aktivieren
        textField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {

            // Suche alle PDFs die den gesuchten String aus textField im Dateinamen enthalten und speichere sie in seachedDataLines
            ObservableList<PDFSelectionLines> searchedDataLines = FXCollections.observableArrayList();
            for (PDFSelectionLines l: dataLines) {
                if(l.name.getValue().toLowerCase().contains(textField.getText().toLowerCase()))
                    {searchedDataLines.add(l);}
            }

            // Lade die gefundenen Daten in die Tabelle
            table.setItems(searchedDataLines);

            // Sollte der UP-Key gedrückt worden sein, dann setze den Cursor ans Ende des Suchbegriffes im Textfeld,
            // da der Cursor sonst an den Anfang des Suchbegriffes springt
            if(event.getCode() == KeyCode.UP){
                textField.positionCaret(textField.getText().length());
            }

            // Sollte ein Enter gedrückt worden sein, öffne die PDF-Datei, die ganz oben ist und dank des vorherigen
            // Befehls nun makiert ist.
            if(event.getCode() == KeyCode.ENTER){
               PDFSelectionLines selectedLine =  (PDFSelectionLines) table.getSelectionModel().getSelectedItem();
               openPDFViewer(new File(PDF_DIRECTORY.concat(selectedLine.getName())));
            }
        });



        // Displaye den Dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(bundle.getString("found_pdf"));
        alert.getDialogPane().setContent(vbox);
        alert.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event-> {

            // Falls ESC gedrückt wurde schließe das Fenster, da es sonst keine Shortcut Close-Operation gibt.
            if(event.getCode() == KeyCode.ESCAPE)
                {alert.close();}

            // Hole den Focus des Cursors auf das Suchfeld textField
            textField.requestFocus();
            textField.positionCaret(textField.getText().length());
        });

        // Lösche die Default Close-Operation vom Yes-Button um ein Enter-Hit für die Suche fangen zu können.
        Button yesButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        yesButton.setDefaultButton(false);

        // Zeige den Dialog
        alert.showAndWait();
    }


    private void openPDFViewer (File file){
        new PDFViewer(file);
    }



    // Inline Class für die Generierung der Tabellenzeilen im Auswahldialog
    public class PDFSelectionLines {

        private SimpleStringProperty name;

        PDFSelectionLines(String name) {
            this.name = new SimpleStringProperty(name);
        }

        public String getName() {return this.name.get();}
    }
}
