package com.github.bachelorpraktikum.visualisierbar.view.graph.adapter;

import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import java.util.function.Function;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.geometry.Point2D;
import javax.annotation.Nonnull;

/**
 * Represents an adapter for transforming the abstract coordinates of the model package to real
 * coordinates for visualization.
 */
public interface CoordinatesAdapter {

    /**
     * Gets the length of the shortest edge in the graph. Should be used to determine the size of
     * elements in the graph, like the size of nodes or the width of edges.<br> The length returned
     * by this method is the real length corresponding to the real coordinates returned by {@link
     * #apply(Node)}.
     *
     * @return the length of the shortest edge
     */
    double getCalibrationBase();

    /**
     * Transforms the Coordinates of a Node in the Model to a Point in the Graph by applying the offsets
     * @param node the Model Node to transform the Coordinates of
     * @return the transformed Point
     */
    @Nonnull
    Point2D apply(@Nonnull Node node);

    /**
     * Transforms a Point in the Graph to Coordinates in the Model by updating and applying the offsets
     * @return the transformed Coordinates
     */
    Coordinates reverse(@Nonnull Point2D point);

    //BooleanProperty movedProperty();
}
