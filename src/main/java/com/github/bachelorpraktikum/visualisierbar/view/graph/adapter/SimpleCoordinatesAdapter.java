package com.github.bachelorpraktikum.visualisierbar.view.graph.adapter;

import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;

/**
 * A simple implementation of {@link CoordinatesAdapter} which does not respect the real length of
 * edges.
 */
public final class SimpleCoordinatesAdapter implements CoordinatesAdapter {

    //private IntegerProperty OffsetX;
    //private IntegerProperty OffsetY;

    public SimpleCoordinatesAdapter(){
        super();
        //OffsetX=new SimpleIntegerProperty();
        //OffsetY=new SimpleIntegerProperty();
        //OffsetX.setValue(0);
        //OffsetY.setValue(0);

    }

    @Override
    public double getCalibrationBase() {
        return 1;
    }

    @Nonnull
    @Override
    public Point2D apply(@Nonnull Node node) {
        Coordinates coordinates = node.getCoordinates();
        //if (coordinates.getX()<0) OffsetX.setValue(-coordinates.getX());
        //if (coordinates.getY()<0) OffsetY.setValue(-coordinates.getY());
        //return new Point2D(coordinates.getX()+OffsetX.getValue(), coordinates.getY()+OffsetY.getValue());
        return new Point2D(coordinates.getX(), coordinates.getY());
    }

    @Override
    public Coordinates reverse(@Nonnull Point2D point) {
        //if (point.getX()<0)
        //return new Coordinates((int) point.getX()-OffsetX.getValue(), (int) point.getY()-OffsetY.getValue());
        return new Coordinates((int) point.getX(), (int) point.getY());
    }
    /*
    @Override
    public void setOffsetX(int x) {
        OffsetX.setValue(x);
    }

    @Override
    public void setOffsetY(int y) {
        OffsetY.setValue(y);
    }

    public IntegerProperty OffsetXproperty() {
        return OffsetX;
    }

    @Override
    public IntegerProperty OffsetYproperty() {
        return OffsetY;
    }
    */
}
