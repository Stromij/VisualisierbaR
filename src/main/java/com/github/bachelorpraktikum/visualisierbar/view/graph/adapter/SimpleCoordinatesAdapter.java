package com.github.bachelorpraktikum.visualisierbar.view.graph.adapter;

import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;
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
        return new Point2D(coordinates.getX(), coordinates.getY());
    }

    @Override
    public Coordinates reverse(@Nonnull Point2D point) {
        return new Coordinates((int) point.getX(), (int) point.getY());
    }

}
