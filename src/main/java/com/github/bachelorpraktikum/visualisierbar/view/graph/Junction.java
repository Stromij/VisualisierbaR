package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.moveable;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import javax.annotation.Nonnull;
import javafx.scene.Cursor;



public final class Junction extends SingleGraphShapeBase<Node, Circle> implements com.github.bachelorpraktikum.visualisierbar.view.moveable {

    private static final double CALIBRATION_COEFFICIENT = 0.1;
    private double mousePressedX = -1;
    private double mousePressedY = -1;

    private boolean moveable;

    Junction(Node node, CoordinatesAdapter adapter) {
        super(node, adapter);

        setMoveable(false);

        this.getShape().setOnMouseReleased((event) -> {
            mousePressedX = -1;
            mousePressedY = -1;
            Circle c = (Circle)event.getSource();
            c.setTranslateX(Math.round(c.getTranslateX()));
            c.setTranslateY(Math.round(c.getTranslateY()));


            System.out.println(("Node X:" + this.getRepresented().getCoordinates().toPoint2D().getX()+" " + "Node Y:" + this.getRepresented().getCoordinates().toPoint2D().getY() ));
            System.out.println("X:"+ (c.getCenterX()+c.getTranslateX())+" "+"Y:"+ (c.getCenterY()+c.getTranslateY()));
            c.setCenterX(c.getCenterX()+c.getTranslateX());
            c.setCenterY(c.getCenterY()+c.getTranslateY());
            this.getRepresented().setCoordinates(new Coordinates(((int) c.getCenterX()),(int) c.getCenterY()));
            c.setTranslateX(0);
            c.setTranslateY(0);
            this.getRepresented().movedProperty().setValue(!this.getRepresented().movedProperty().getValue());
            ////////////
            //this.getRepresented().getElements().forEach((a)->{a.});
        });
        this.getShape().setOnMouseDragged((t) -> {


            if (!t.isPrimaryButtonDown() || !moveable) {
                return;
            }

            if (mousePressedX == -1 && mousePressedY == -1) {
                mousePressedX = t.getX();
                mousePressedY = t.getY();
            }
            double offsetX=(t.getX() - mousePressedX);
            double offsetY=(t.getY() - mousePressedY);



            Circle c = (Circle) (t.getSource());

            c.setTranslateX(c.getTranslateX()+offsetX);
            c.setTranslateY(c.getTranslateY()+offsetY);


            //this.getRepresented().movedProperty().setValue(!this.getRepresented().movedProperty().getValue());

            t.consume();
        });
    }


    @Override
    protected void relocate(Circle shape) {
        Node node = getRepresented();
        CoordinatesAdapter adapter = getCoordinatesAdapter();
        Point2D position = adapter.apply(node);
        shape.setCenterX(position.getX());
        shape.setCenterY(position.getY());
    }

    @Override
    protected void resize(Circle shape) {
        double radius = getCalibrationBase() * CALIBRATION_COEFFICIENT;
        shape.setRadius(radius);
    }

    @Override
    protected void initializedShape(Circle circle) {
        TooltipUtil.install(circle, new Tooltip(getRepresented().getName()));
    }

    @Nonnull
    @Override
    public Circle createShape() {
        return new Circle();
    }

    @Override
    protected javafx.scene.Node createHighlight(Circle circle) {
        return createCircleHighlight(circle);
    }

    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
        if (moveable == true) this.getShape().setCursor(Cursor.HAND);
        else  this.getShape().setCursor(Cursor.DEFAULT);
    }

    @Override
    public boolean getMoveable() {
        return moveable;
    }

}
