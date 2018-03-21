package com.github.bachelorpraktikum.visualisierbar.view.graph.elements;

import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.model.Switch;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import java.util.HashMap;

final class WeichenpunktElement extends ElementBase<Polygon> {
    private final Switch aSwitch;
    private HashMap<Element,Circle> highlightCircles;
    WeichenpunktElement(Switch aSwitch, Node node, CoordinatesAdapter adapter) {
        super(aSwitch.getElements(), node, adapter);
        this.aSwitch = aSwitch;
    }
    private Element getMainElement() {
        return aSwitch.getMainElement();
    }
    @Nonnull
    @Override
    public Shape getShape(@Nonnull Element element) {
        return getShape();
    }

    @Override
    protected void relocate(Polygon shape) {
            CoordinatesAdapter adapter = getCoordinatesAdapter();
            Point2D[] others = new Point2D[2];
            int index = 0;
            for (Element element : aSwitch.getElements()) {
                if (!element.equals(getMainElement())) {
                    others[index++] = adapter.apply(element.getNode());
                }
            }
            if(getHighlight()!=null){
                for (Element element : getRepresentedObjects()) {
                    Point2D nodePos = getCoordinatesAdapter().apply(element.getNode());
                    Circle nodeCirlce= highlightCircles.get(element);
                    nodeCirlce.setLayoutX(nodePos.getX());
                    nodeCirlce.setLayoutY(nodePos.getY());
        }}

            Point2D nodePos = new Point2D(getMainElement().getNode().getCoordinates().getX(), getMainElement().getNode().getCoordinates().getY());
            Point2D vec1 = others[0].subtract(nodePos);
            Point2D vec2 = others[1].subtract(nodePos);

            Point2D start = nodePos
                    .add(vec1.add(vec2).normalize().multiply(0.1 * getCalibrationBase()));
            Point2D fin1 = start.add(vec1.normalize().multiply(0.3 * getCalibrationBase()));
            Point2D fin2 = start.add(vec2.normalize().multiply(0.3 * getCalibrationBase()));
            ObservableList<Double> points = shape.getPoints();
            points.set(0, start.getX());
            points.set(1, start.getY());
            points.set(2, fin1.getX());
            points.set(3, fin1.getY());
            points.set(4, fin2.getX());
            points.set(5, fin2.getY());
    }

    @Override
    protected Point2D getOffset() {
        return Point2D.ZERO;
    }

    @Override
    protected void resize(Polygon shape) {
    }

    @Override
    protected javafx.scene.Node createHighlight(Polygon polygon) {
        Group highlight = new Group();
        highlightCircles= new HashMap<>(getRepresentedObjects().size());
        for (Element element : getRepresentedObjects()) {
            Node node = element.getNode();
            Point2D nodePos = getCoordinatesAdapter().apply(node);
            Circle nodeCircle = new Circle(0.1 * getCalibrationBase());
            nodeCircle.setLayoutX(nodePos.getX());
            nodeCircle.setLayoutY(nodePos.getY());
            highlightCircles.put(element, nodeCircle);

            highlight.getChildren().add(createCircleHighlight(nodeCircle));
        }
        return highlight;
    }

    @Nonnull
    @Override
    protected Polygon createShape() {
        Polygon shape= new Polygon();
        CoordinatesAdapter adapter = getCoordinatesAdapter();
        Point2D[] others = new Point2D[2];
        int index = 0;
        for (Element element : aSwitch.getElements()) {
            if (!element.equals(getMainElement())) {
                others[index++] = adapter.apply(element.getNode());
            }
        }
        Point2D nodePos = new Point2D(getMainElement().getNode().getCoordinates().getX(), getMainElement().getNode().getCoordinates().getY());
        Point2D vec1 = others[0].subtract(nodePos);
        Point2D vec2 = others[1].subtract(nodePos);

        Point2D start = nodePos
                .add(vec1.add(vec2).normalize().multiply(0.1 * getCalibrationBase()));
        Point2D fin1 = start.add(vec1.normalize().multiply(0.3 * getCalibrationBase()));
        Point2D fin2 = start.add(vec2.normalize().multiply(0.3 * getCalibrationBase()));
        ObservableList<Double> points = shape.getPoints();
        points.addAll(start.getX(), start.getY(),
                fin1.getX(), fin1.getY(),
                fin2.getX(), fin2.getY());
        return shape;
    }
}
