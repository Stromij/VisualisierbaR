package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.GraphObject;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class GraphShapeBase<R extends GraphObject<?>, S extends Node>
    implements GraphShape<R> {

    private static final double HIGHLIGHT_FACTOR = 1.5;
    private static final double HIGHLIGHT_STROKE_WIDTH = 0.05;
    private final CoordinatesAdapter adapter;
    private final BooleanProperty highlighted = new SimpleBooleanProperty();
    private List<javafx.beans.value.ChangeListener> listeners=new ArrayList<>(1);
    @Nullable
    private S shape;
    @Nullable
    private Node highlight;
    @Nullable
    private Group full;
    protected GraphShapeBase(CoordinatesAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * create and initialze a shape
     * @return shape
     */
    private S initializeShape() {
        S shape = createShape();
        resize(shape);
        relocate(shape);
        return shape;
    }
    protected void initializedShape(S shape) {}

    @Nonnull
    @Override
    public final S getShape() {
        if (shape == null) {
            shape = initializeShape();
            initializedShape(shape);
            highlight = createHighlight(shape);
            highlight.visibleProperty().bind(highlightedProperty());
        }

        return shape;
    }

    @Nonnull
    @Override
    public Node getFullNode() {
        if (full == null) {
            full = new Group();
            full.getChildren().addAll(getShape(), highlight);
        }
        return full;
    }

    @Override
    public BooleanProperty highlightedProperty() {
        return highlighted;
    }

    public Node getHighlight() {return highlight;}

    /**
     * get a CoordinateAdapter
     * @return adapter
     */
    protected final CoordinatesAdapter getCoordinatesAdapter() {
        return adapter;
    }

    /**
     * get a CalibrationBase
     * @return calibrationBase
     */
    protected final double getCalibrationBase() {
        return getCoordinatesAdapter().getCalibrationBase();
    }

    /**
     * get Offset
     * @return offset
     */
    protected Point2D getOffset() {
        return new Point2D(0.4, 0.4).multiply(getCalibrationBase());
    }
    protected abstract void relocate(S shape);
    protected abstract void resize(S shape);
    @Nonnull
    protected abstract S createShape();
    protected abstract Node createHighlight(S node);

    /**
     * create circle Highlight of node
     * @param node node for which a circle highlight is created
     * @return circle
     */
    protected Node createCircleHighlight(Node node) {
        Circle circle = new Circle();
        Bounds nodeBounds = node.getBoundsInParent();
        circle.setCenterY(nodeBounds.getMinY() + nodeBounds.getHeight() / 2);
        circle.setCenterX(nodeBounds.getMinX() + nodeBounds.getWidth() / 2);
        circle.setRadius(
            Math.max(nodeBounds.getWidth(), nodeBounds.getHeight()) * HIGHLIGHT_FACTOR
        );

        javafx.beans.value.ChangeListener XlocationListener = ((observable, oldValue, newValue) -> {
            Bounds nodeBounds2 = node.getBoundsInParent();
            circle.setCenterY(nodeBounds2.getMinY() + nodeBounds2.getHeight() / 2);
            circle.setCenterX(nodeBounds2.getMinX() + nodeBounds2.getWidth() / 2);
            circle.setRadius(
                    Math.max(nodeBounds2.getWidth(), nodeBounds2.getHeight()) * HIGHLIGHT_FACTOR
            );
        });
        listeners.add(XlocationListener);
        node.layoutXProperty().addListener(new WeakChangeListener<>(XlocationListener));



        javafx.beans.value.ChangeListener YlocationListener = ((observable, oldValue, newValue) -> {
            Bounds nodeBounds2 = node.getBoundsInParent();
            circle.setCenterY(nodeBounds2.getMinY() + nodeBounds2.getHeight() / 2);
            circle.setCenterX(nodeBounds2.getMinX() + nodeBounds2.getWidth() / 2);
            circle.setRadius(
                    Math.max(nodeBounds2.getWidth(), nodeBounds2.getHeight()) * HIGHLIGHT_FACTOR
            );
        });
        node.layoutYProperty().addListener(new WeakChangeListener<>(YlocationListener));
        listeners.add(YlocationListener);


        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.BLUE);
        circle.setStrokeWidth(
            HIGHLIGHT_STROKE_WIDTH * getCalibrationBase()
        );
        circle.setMouseTransparent(true);
        return circle;
    }

    Node createRectangleHighlight(Node node) {
        Rectangle rectangle = new Rectangle();
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.BLUE);
        rectangle.setStrokeWidth(HIGHLIGHT_STROKE_WIDTH * getCalibrationBase());
        Bounds nodeBounds = node.getBoundsInParent();
        double width = nodeBounds.getWidth() * HIGHLIGHT_FACTOR;
        double height = nodeBounds.getHeight() * HIGHLIGHT_FACTOR;
        rectangle.setX(nodeBounds.getMinX() - (width - nodeBounds.getWidth()) / 2);
        rectangle.setY(nodeBounds.getMinY() - (height - nodeBounds.getHeight()) / 2);
        rectangle.setWidth(width);
        rectangle.setHeight(height);

        javafx.beans.value.ChangeListener XlocationListener = ((observable, oldValue, newValue) -> {
            Bounds nodeBounds2 = node.getBoundsInParent();
            double width2 = nodeBounds2.getWidth() * HIGHLIGHT_FACTOR;
            double height2 = nodeBounds2.getHeight() * HIGHLIGHT_FACTOR;
            rectangle.setX(nodeBounds2.getMinX() - (width2 - nodeBounds2.getWidth()) / 2);
            rectangle.setY(nodeBounds2.getMinY() - (height2 - nodeBounds2.getHeight()) / 2);
            rectangle.setWidth(width2);
            rectangle.setHeight(height2);
        });
        node.layoutXProperty().addListener(new WeakChangeListener<>(XlocationListener));
        listeners.add(XlocationListener);

        javafx.beans.value.ChangeListener YlocationListener = ((observable, oldValue, newValue) -> {
            Bounds nodeBounds2 = node.getBoundsInParent();
            double width2 = nodeBounds2.getWidth() * HIGHLIGHT_FACTOR;
            double height2 = nodeBounds2.getHeight() * HIGHLIGHT_FACTOR;
            rectangle.setX(nodeBounds2.getMinX() - (width2 - nodeBounds2.getWidth()) / 2);
            rectangle.setY(nodeBounds2.getMinY() - (height2 - nodeBounds2.getHeight()) / 2);
            rectangle.setWidth(width2);
            rectangle.setHeight(height2);
        });
        node.layoutYProperty().addListener(new WeakChangeListener<>(YlocationListener));
        listeners.add(YlocationListener);



        return rectangle;
    }
}
