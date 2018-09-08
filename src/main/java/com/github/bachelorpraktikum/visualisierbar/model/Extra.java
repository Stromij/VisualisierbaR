package com.github.bachelorpraktikum.visualisierbar.model;

public class Extra {

    //Ne5 ne1 = new Ne5(60, "nesig1", False, el056, "ne1");

    private int number;
    private String additionalName;
    private String bool;

    public Extra(int number, String additionalName, String bool)
        {this.number = number;
         this.additionalName = additionalName;
         this.bool = bool;
        }

    public int getNumber() {return this.number;}

    public String getAdditionalName() {return this.additionalName;}

    public String getBool() {return this.bool;}

}
