package com.github.bachelorpraktikum.visualisierbar.view.train;

import com.github.bachelorpraktikum.visualisierbar.config.ConfigFile;
import com.github.bachelorpraktikum.visualisierbar.model.Context;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.model.train.Train;
import com.github.bachelorpraktikum.visualisierbar.view.DataSourceHolder;
import com.github.bachelorpraktikum.visualisierbar.view.Highlightable;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

public final class TrainView implements Highlightable {

    private static final double TRAIN_WIDTH = 0.4;
    private static final double HIGHLIGHT_FACTOR = 0.6;

    private final Train train;
    private final IntegerProperty timeProperty;
    private final Function<Node, Point2D> coordinatesTranslator;
    private final double calibrationBase;
    private final Path path;
    private final Rectangle highlightRectangle;
    private BooleanProperty highlightedProperty;

    private static final Paint[] COLORS = ConfigFile.getInstance().getTrainColors();

    public TrainView(Train train, Graph graph) {
        this.train = train;
        this.coordinatesTranslator = (a)->graph.getCoordinatesAdapter().apply(a);
        this.calibrationBase = graph.getCoordinatesAdapter().getCalibrationBase();
        this.timeProperty = new SimpleIntegerProperty(0);
        this.highlightedProperty = new SimpleBooleanProperty(false);

        this.highlightRectangle = new Rectangle();
        highlightRectangle.setFill(Color.TRANSPARENT);
        highlightRectangle.setStrokeWidth(0.05 * calibrationBase);
        highlightRectangle.visibleProperty().bind(highlightedProperty());
        highlightRectangle.setMouseTransparent(true);
        graph.getGroup().getChildren().add(highlightRectangle);

        this.path = new Path();
        path.setStrokeWidth(TRAIN_WIDTH * calibrationBase);
        path.setStroke(train.getColor());
        path.setStrokeLineCap(StrokeLineCap.BUTT);
        graph.getGroup().getChildren().add(path);
        path.toBack();

        Context context = DataSourceHolder.getInstance().getContext();
        ObservableBooleanValue visibleBinding = Bindings.createBooleanBinding(
            () -> train.isVisible(path.getBoundsInParent()),
            train.visibleStateProperty()
        );
        context.addObject(visibleBinding);
        path.visibleProperty().bind(visibleBinding);

        timeProperty.addListener((observable, oldValue, newValue) ->
            updateTrain(newValue.intValue())
        );
        updateTrain(0);

        TooltipUtil.install(path, new Tooltip(train.getReadableName() + " " + train.getLength()));
    }

    public void setOnMouseClicked(EventHandler<? super MouseEvent> eventHandler) {
        path.setOnMouseClicked(eventHandler);
    }

    public Train getTrain() {
        return train;
    }

    public IntegerProperty timeProperty() {
        return timeProperty;
    }

    private void updateTrain(int time) {
        path.getElements().clear();
        Train.State state = train.getState(time);
        if (!state.isInitialized()) {
            highlightRectangle.setStroke(Color.TRANSPARENT);
            return;
        } else {
            highlightRectangle.setStroke(Color.BLUE);
        }
        Train.Position trainPosition = state.getPosition();
        List<Point2D> points = trainPosition.getPositions(coordinatesTranslator);
        List<PathElement> elements = new LinkedList<>();

        Iterator<Point2D> iterator = points.iterator();
        Point2D start = iterator.next();
        elements.add(new MoveTo(start.getX(), start.getY()));

        while (iterator.hasNext()) {
            Point2D point = iterator.next();
            elements.add(new LineTo(point.getX(), point.getY()));
        }

        path.getElements().addAll(elements);

        if (state.isTerminated()) {
            path.setStroke(Color.GRAY);
        } else {
            path.setStroke(getTrain().getColor());
        }

        Bounds pathBounds = path.getBoundsInParent();
        double width = pathBounds.getWidth() * HIGHLIGHT_FACTOR;
        double height = pathBounds.getHeight() * HIGHLIGHT_FACTOR;
        highlightRectangle.setX(pathBounds.getMinX() - (width - pathBounds.getWidth()) / 2);
        highlightRectangle.setY(pathBounds.getMinY() - (height - pathBounds.getHeight()) / 2);
        highlightRectangle.setWidth(width);
        highlightRectangle.setHeight(height);
    }

    @Override
    public BooleanProperty highlightedProperty() {
        return highlightedProperty;
    }
}
