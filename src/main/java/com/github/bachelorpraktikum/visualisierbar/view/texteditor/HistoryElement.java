package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import java.io.File;

public class HistoryElement {

    private File pathToFile;
    private StringBuffer doc;
    private int cursorPosition;


    public HistoryElement(File pathToFile, StringBuffer doc, int cursorPosition)
        {this.pathToFile = pathToFile;
         this.doc = doc;
         this.cursorPosition = cursorPosition;
        }

    public File getFile()
        {return pathToFile;}

    public StringBuffer getDocument()
        {return doc;}

    public int getCursorPosition()
        {return cursorPosition;}
}
