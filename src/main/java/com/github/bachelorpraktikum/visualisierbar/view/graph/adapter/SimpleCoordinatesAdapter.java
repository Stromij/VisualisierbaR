package com.github.bachelorpraktikum.visualisierbar.view.graph.adapter;

import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A simple implementation of {@link CoordinatesAdapter} which does not respect the real length of
 * edges.
 */
public final class SimpleCoordinatesAdapter implements CoordinatesAdapter {


    // Offsets that get applied to the model in order to keep all coordinates positive  i.e. if a node gets dragged to -1
    // every node adds 1 to its coordinates
    private int OffsetX;
    private int OffsetY;
    //Blinker that signifies change in the offsets
    //private BooleanProperty movedProperty;


    public SimpleCoordinatesAdapter(){
        super();
        //OffsetX=new SimpleIntegerProperty();
        //OffsetY=new SimpleIntegerProperty();
        //OffsetX.setValue(0);
        //OffsetY.setValue(0);
        OffsetX=0;
        OffsetY=0;
        //movedProperty = new SimpleBooleanProperty(false);

    }

    @Override
    public double getCalibrationBase() {
        return 1;
    }

    @Nonnull
    @Override
    public Point2D apply(@Nonnull Node node) {
        Coordinates coordinates = node.getCoordinates();
        //if (coordinates.getX()<0) OffsetX = coordinates.getX();
        //if (coordinates.getY()<0) OffsetY = coordinates.getY();
        return new Point2D(coordinates.getX()+ OffsetX, coordinates.getY()+OffsetY);
        //return new Point2D(coordinates.getX(), coordinates.getY());
    }

    @Override
    public Coordinates reverse(@Nonnull Point2D point) {
        if (point.getX()<0) OffsetX=(int)point.getX();
        if (point.getY()<0) OffsetY=(int)point.getY();
        //if( (point.getX()<0) ||  (point.getY()<0)) this.movedProperty.setValue(!movedProperty.getValue());

        //return new Coordinates((int) point.getX()-OffsetX.getValue(), (int) point.getY()-OffsetY.getValue());
        return new Coordinates((int) point.getX()-OffsetX, (int) point.getY()-OffsetY);
    }

    @Override
    public void setOffsetX(int x) {
        OffsetX=x;
    }

    @Override
    public void setOffsetY(int y) {
        OffsetY=y;
    }
/*
    public BooleanProperty movedProperty() {
        return movedProperty;
    }
/*
    @Override
    public IntegerProperty OffsetYproperty() {
        return OffsetY;
    }
*/
}
