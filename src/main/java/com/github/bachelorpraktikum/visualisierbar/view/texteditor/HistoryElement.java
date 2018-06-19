package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import java.io.File;

public class HistoryElement {

    private File pathToFile;
    private StringBuffer doc;


    public HistoryElement(File pathToFile, StringBuffer doc)
        {this.pathToFile = pathToFile;
         this.doc = doc;
        }

    public File getFile()
        {return pathToFile;}

    public StringBuffer getDocument()
        {return doc;}
}
