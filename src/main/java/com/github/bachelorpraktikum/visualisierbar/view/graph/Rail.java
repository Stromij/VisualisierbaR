package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Edge;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javax.annotation.Nonnull;

final class Rail extends SingleGraphShapeBase<Edge, Line> {

    private static final double CALIBRATION_COEFFICIENT = 0.05;

    protected Rail(Edge edge, CoordinatesAdapter adapter) {
        super(edge, adapter);
    }

    @Override
    public void relocate(Line shape) {
        CoordinatesAdapter adapter = getCoordinatesAdapter();
        Point2D start = adapter.apply(getRepresented().getNode1());
        Point2D end = adapter.apply(getRepresented().getNode2());
        if(getRepresented().getLength()<0) shape.setStroke(Color.RED);
        else shape.setStroke(Color.BLACK);
        shape.setStartX(start.getX());
        shape.setStartY(start.getY());
        shape.setEndX(end.getX());
        shape.setEndY(end.getY());

        //System.out.println(getRepresented().getLength());
    }

    @Override
    protected void resize(Line line) {
        line.setStrokeWidth(getCalibrationBase() * CALIBRATION_COEFFICIENT);
    }

    @Override
    protected void initializedShape(Line line) {
        TooltipUtil.install(line,
            new Tooltip(getRepresented().getName() + " " + getRepresented().getLength() + "m"));
    }

    @Nonnull
    @Override
    public Line createShape() {
        getRepresented().getNode1().movedProperty().addListener((observable, oldValue, newValue) -> {
            relocate(this.getShape());

        });
        getRepresented().getNode2().movedProperty().addListener((observable, oldValue, newValue) -> {
            relocate(this.getShape());

        });
        return new Line();
    }

    @Override
    protected Node createHighlight(Line node) {
        return createRectangleHighlight(node);
    }
}
