package com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
                    PDFViewer pdfViewer = new PDFViewer(new File(PDF_DIRECTORY.concat(tableRow.getItem().getName())));
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


        // Füge dem Fenster einen Listener für beliebigen Key hinzu, um Schnellsuche zu aktivieren
        textField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {

            //TODO catch up and down Keys for navigation to table -> Usability

            ObservableList<PDFSelectionLines> searchedDataLines = FXCollections.observableArrayList();
            for (PDFSelectionLines l: dataLines) {
                if(l.name.getValue().toLowerCase().contains(textField.getText().toLowerCase()))
                    {searchedDataLines.add(l);}
            }

            table.setItems(searchedDataLines);
        });



        // Displaye den Dialog
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(bundle.getString("found_pdf"));
        alert.getDialogPane().setContent(vbox);
        alert.getDialogPane().addEventHandler(KeyEvent.KEY_PRESSED, event-> {
            textField.requestFocus();
        });
        alert.showAndWait();
    }





    // Inline Class für die Generierung der Tabellenzeilen im Auswahldialog
    public class PDFSelectionLines {

        private SimpleStringProperty name;

        public PDFSelectionLines(String name) {
            this.name = new SimpleStringProperty(name);
        }

        public String getName() {return this.name.get();}
    }
}
