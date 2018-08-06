package com.github.bachelorpraktikum.visualisierbar.view.texteditor;

import javax.annotation.Nonnull;
import java.io.File;

public class HistoryElement {

    @Nonnull
    private File pathToFile;
    @Nonnull
    private StringBuffer doc;

    private int cursorPosition;


    public HistoryElement(@Nonnull File pathToFile, @Nonnull StringBuffer doc, int cursorPosition)
        {this.pathToFile = pathToFile;
         this.doc = doc;
         this.cursorPosition = cursorPosition;
        }

    @Nonnull
    public File getFile()
        {return pathToFile;}

    @Nonnull
    public StringBuffer getDocument()
        {return doc;}

    public int getCursorPosition()
        {return cursorPosition;}
}
