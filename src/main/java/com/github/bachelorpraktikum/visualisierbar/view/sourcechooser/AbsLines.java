package com.github.bachelorpraktikum.visualisierbar.view.sourcechooser;

import javafx.beans.property.SimpleStringProperty;

public class AbsLines {
    private SimpleStringProperty line;
    private SimpleStringProperty first;
    private SimpleStringProperty second;

    public AbsLines(int line, String first, String second)
        {this.line = new SimpleStringProperty(String.valueOf(line));
         this.first = new SimpleStringProperty(first);
         this.second = new SimpleStringProperty(second);
        }

    public String getLine() {return this.line.get();}

    public String getFirst() {return this.first.get();}

    public String getSecond() {return this.second.get();}
}

