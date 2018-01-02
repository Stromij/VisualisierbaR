package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An object in the simulation which has an unique name and can be represented as a Shape.
 *
 * @param <S> the type of Shape {@link #createShape()} will return
 */
public interface GraphObject<S extends Shape> extends Shapeable<S> {

    /**
     * Gets the unique name of this object.
     *
     * @return the name
     */
    @Nonnull
    String getName();

    /**
     * Gets the Graph this object is in
     * @return the graph
     */
    @Nullable
    Graph getGraph();

    /**
     * sets the graph of this object
     * @param graph the graph this object is in
     */
    @Nullable
    void setGraph(Graph graph);



    /**
     * <p>Gets a human readable name for this object.</p>
     *
     * <p>The default implementation calls {@link #getName()}.</p>
     *
     * @return the readable name
     */
    @Nonnull
    default String getReadableName() {
        return getName();
    }

}
