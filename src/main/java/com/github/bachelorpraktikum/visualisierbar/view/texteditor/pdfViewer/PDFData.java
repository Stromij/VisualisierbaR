package com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;

public class PDFData {

    private final String type;
    private final String specification;
    private final String csvFile;

    private ArrayList<String[]> data;
    private ObservableList<PDFDataLines> dataLines;




    public PDFData(String type, String specification) throws IOException {
       this.type = type;
       this.specification = specification;

       this.csvFile = "src/main/csv/github/bachelorpraktikum/visualisierbar/database/".concat(type).concat(".csv");

       BufferedReader br = new BufferedReader(new FileReader(csvFile));


       String line;
       data = new ArrayList<>();
       dataLines = FXCollections.observableArrayList();
       // Suche geforderte Richtline(n?) in CSV und speichere ihre Daten in data
       // und speichere sie außerdem in einer PDFDataLines, um sie dem User später als Tabelle präsentieren zu können
       while ((line = br.readLine()) != null) {
            String[] spec = line.split(",");
            if(spec[0].equals(specification) || spec[1].equals(specification))
                {data.add(spec);
                 dataLines.add(new PDFDataLines(spec[0],
                             spec[1],
                             Integer.parseInt(spec[2]),
                             Integer.parseInt(spec[3]),
                             dataLines.size()));
                }
       }

       // geforderte Richtlinie konnte nicht gefunden werden
       if(data.size() == 0)
            {throw new IOException("Cannot find guideline!");}
    }



    public File getLocation(int count)
        {return new File(data.get(count)[6]);}



    public String getGuideline(int count)
        {return data.get(count)[0];}



    public String getGuidelineName(int count)
        {return data.get(count)[1];}



    public int getStartPage(int count)
        {return Integer.parseInt(data.get(count)[2]);}



    public int getEndPage(int count)
        {return Integer.parseInt(data.get(count)[3]);}



    public int getStartY(int count)
        {return Integer.parseInt(data.get(count)[4]);}



    public int getEndY(int count)
        {return Integer.parseInt(data.get(count)[5]);}



    public ArrayList<String[]> getAllMatches() {
        return data;
    }


    public ObservableList<PDFDataLines> getDataLines() {
        return dataLines;
    }
}
