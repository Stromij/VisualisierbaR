package com.github.bachelorpraktikum.visualisierbar.view.texteditor.pdfViewer;

import javafx.beans.property.SimpleStringProperty;

public class PDFDataLines {

    private SimpleStringProperty guideline;
    private SimpleStringProperty guidelineName;
    private SimpleStringProperty startPage;
    private SimpleStringProperty endPage;
    private SimpleStringProperty length;
    private int indiz;

    public PDFDataLines(String guideline, String guidelineName, int startPage, int endPage, int indiz)
        {this.guideline = new SimpleStringProperty(guideline);
         this.guidelineName = new SimpleStringProperty(guidelineName);
         this.startPage = new SimpleStringProperty(String.valueOf(startPage));
         this.endPage = new SimpleStringProperty(String.valueOf(endPage));
         this.length = new SimpleStringProperty(String.valueOf((endPage - startPage) + 1));
         this.indiz = indiz;
        }


    public String getGuideline() {return this.guideline.get();}

    public String getGuidelineName() {return this.guidelineName.get();}

    public String getStartPage() {return this.startPage.get();}

    public String getEndPage() {return this.endPage.get();}

    public String getLength() {return this.length.get();}

    public int getIndiz() {return this.indiz;}
}
