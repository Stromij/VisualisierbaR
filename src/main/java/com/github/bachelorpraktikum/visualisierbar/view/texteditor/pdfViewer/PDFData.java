package com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer;

import java.io.*;

public class PDFData {

    private final String type;
    private final String specification;
    private final String csvFile;


    private String[] data;

    public PDFData(String type, String specification) throws IOException {
       this.type = type.toLowerCase();
       this.specification = specification;

       this.csvFile = "src/main/csv/github/bachelorpraktikum/visualisierbar/database/Richtlinie.csv";

       BufferedReader br = new BufferedReader(new FileReader(csvFile));


       String line;
       data = null;
       // Suche geforderte Richtline in CSV und speichere ihre Daten in data
       while ((line = br.readLine()) != null) {
            String[] spec = line.split(",");
            if(spec[0].equals(specification))
                {data = spec; break;}
       }

       // geforderte Richtlinie konnte nicht gefunden werden
       if(data == null)
            {throw new IOException("Cannot find guideline!");}
    }


    public File getLocation()
        {return new File(data[5]);}

    public String getGuideline()
        {return data[0];}

    public int getStartPage()
        {return Integer.parseInt(data[1]);}

    public int getEndPage()
        {return Integer.parseInt(data[2]);}

    public int getStartY()
        {return Integer.parseInt(data[3]);}

    public int getEndY()
        {return Integer.parseInt(data[4]);}

}
