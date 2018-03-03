package com.github.bachelorpraktikum.visualisierbar.view.graph.elements;

import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;

final class CompositeElement extends ElementBase<Group> {

    private static final double MAX_SHAPE_WIDTH = 2.0;
    private static final double ELEMENT_SPACING = 0.7;
    private static final double GESCHWINDIGKEITS_ANZEIGER_WIDTH_FACTOR = 0.5;
    private static final double FOOT_HEIGHT = 0.5;

    private final Map<Element, Shape> shapes;
    private final List<ChangeListener<Element.State>> stateListeners;
    private final int count;

    CompositeElement(List<Element> elements, Node node, CoordinatesAdapter adapter, int count) {
        super(elements, node, adapter);
        this.count = count;
        this.stateListeners = new ArrayList<>(elements.size());
        this.shapes = new LinkedHashMap<>();

        DoubleAdder y = new DoubleAdder();
        elements.stream().sorted(Comparator.comparing(Element::getType)).forEach(element -> {
            Shape shape = createShape(element.getType());
            Bounds bounds = shape.getLayoutBounds();
            shape.relocate(0 - bounds.getWidth() / 2, 0 - bounds.getHeight() / 2);
            shape.setTranslateY(y.doubleValue());
            y.add(shape.getBoundsInParent().getHeight() + ELEMENT_SPACING);
            ChangeListener<Element.State> listener = (observable, oldValue, newValue) -> {
                shape.setFill(newValue.getColor());
            };
            stateListeners.add(listener);
            element.stateProperty().addListener(new WeakChangeListener<>(listener));
            shapes.put(element, shape);
        });
    }

    @Override
    protected Point2D getOffset() {
        Point2D offset = super.getOffset();
        offset = offset.add(offset.multiply(count));
        return offset;
    }

    @Override
    protected void relocate(Group group) {
        Point2D nodePos = getNodePosition().add(getOffset());

        Bounds bounds = group.getBoundsInLocal();
        double x = nodePos.getX() - bounds.getWidth() / 2;
        double y = nodePos.getY() - bounds.getHeight() / 2;

        group.relocate(x, y);

        /*
        shapes.forEach((a,b)->{
            if(a.getDirection()!=null) {

                Point2D p = new Point2D(1, 0);
                Point2D p2 = this.getCoordinatesAdapter().apply(a.getNode());
                Point2D p3 = this.getCoordinatesAdapter().apply(a.getDirection());
                p2= new Point2D(p2.getX()-p3.getX(), p2.getY()-p3.getY());
                double angle = p.angle(p2);
                if(angle>180) angle -=180;
                if(a.getType()== Element.Type.VorSignal)angle += 180;
                b.setRotate(angle-getAngle());
            }
        });
        */

        rotateAccordingToOffset(group);

    }

    @Override
    protected final void rotateAccordingToOffset(Group node, Point2D offset) {

        double angle = new Point2D(0, 1).angle(offset);
        if (offset.getX() > 0) {
            angle = -angle;
        }
        angle += 180;
        node.setRotate(angle);

        Node dir =null;
        for (Element e : shapes.keySet()){
           if(dir == null) dir = e.getDirection();
           if(dir != e.getDirection()) return;
        }

        if(dir !=null) {

            Point2D p = new Point2D(1, 0);
            Point2D p2 = this.getNodePosition();
            Point2D p3 = this.getCoordinatesAdapter().apply(dir);
            p2= new Point2D(p2.getX()-p3.getX(), p2.getY()-p3.getY());
            angle = p.angle(p2);
            if (new Point2D(0,1).dotProduct(p2)<0) angle = - angle;
            angle += 90;
        }
        node.setRotate(angle);
    }



    @Override
    protected void resize(Group shape) {
        resizeNode(shape, MAX_ELEMENT_WIDTH * getCalibrationBase());
    }

    private void resizeNode(javafx.scene.Node node, double maxWidth) {
        Bounds bounds = node.getLayoutBounds();
        double factor = maxWidth / bounds.getWidth();
        node.setScaleX(node.getScaleX() * factor);
        node.setScaleY(node.getScaleY() * factor);
    }

    @Nonnull
    @Override
    public Shape getShape(Element represented) {
        return shapes.get(represented);
    }

    @Nonnull
    @Override
    protected Group createShape() {
        Group group = new Group(shapes.values().stream()
            .collect(Collectors.toList())
        );
        Bounds bounds = group.getLayoutBounds();
        double endY = bounds.getHeight() + FOOT_HEIGHT * getCalibrationBase();
        Line line = new Line(0, 0, 0, endY);
        line.setStrokeWidth(0.16 * getCalibrationBase());
        group.getChildren().add(line);
        line.toBack();

        bounds = group.getLayoutBounds();
        double x = bounds.getWidth() / 2;
        Line bottomLine = new Line(-x, endY, x, endY);
        bottomLine.setStrokeWidth(0.16 * getCalibrationBase());
        group.getChildren().add(bottomLine);
        bottomLine.toBack();
        return group;
    }

    private Shape createShape(Element.Type type) {
        double maxWidth = MAX_SHAPE_WIDTH;
        Shape shape = type.createShape();
        switch (type) {
            case GeschwindigkeitsAnzeiger:
                maxWidth *= GESCHWINDIGKEITS_ANZEIGER_WIDTH_FACTOR;
                break;
            case GeschwindigkeitsVoranzeiger:
                maxWidth *= GESCHWINDIGKEITS_ANZEIGER_WIDTH_FACTOR;
                break;
            default:
                break;
        }
        resizeNode(shape, getCalibrationBase() * maxWidth);
        return shape;
    }

}
