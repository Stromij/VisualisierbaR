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

    public SimpleCoordinatesAdapter(){
        super();
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
        return new Point2D(coordinates.getX(), coordinates.getY());
        //return new Point2D(coordinates.getX(), coordinates.getY());
    }

    @Override
    public Coordinates reverse(@Nonnull Point2D point) {

        return new Coordinates((int) point.getX(), (int) point.getY());
    }

}
