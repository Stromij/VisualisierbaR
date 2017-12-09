package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.moveable;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Point2D;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javax.annotation.Nonnull;

import javafx.scene.Cursor;

import java.util.HashSet;


public final class Junction extends SingleGraphShapeBase<Node, Circle> implements com.github.bachelorpraktikum.visualisierbar.view.moveable {

    private static final double CALIBRATION_COEFFICIENT = 0.1;
    private static HashSet<Junction> selection = new HashSet<>(128);
    private static double mousePressedX = -1;
    private static double mousePressedY = -1;

    private boolean moveable;

    Junction(Node node, CoordinatesAdapter adapter) {
        super(node, adapter);

        setMoveable(false);

        this.getShape().setOnMouseReleased((event) -> {
            mousePressedX = -1;
            mousePressedY = -1;
            selection.forEach((b) -> {
                Circle a = b.getShape();
                //Circle c = (Circle)event.getSource();
                a.setTranslateX(Math.round(a.getTranslateX()));
                a.setTranslateY(Math.round(a.getTranslateY()));

                if (a.getCenterX() + a.getTranslateX() < 0 || a.getCenterY() + a.getTranslateY() < 0) {
                    System.out.println("Coordiantes invalid");
                    a.setTranslateY(0);
                    a.setTranslateX(0);
                    return;
                }

                a.setCenterX(a.getCenterX() + a.getTranslateX());
                a.setCenterY(a.getCenterY() + a.getTranslateY());
                //TODO Coordiantes Adapter??
                b.getRepresented().setCoordinates(new Coordinates(((int) a.getCenterX()), (int) a.getCenterY()));
                a.setTranslateX(0);
                a.setTranslateY(0);
                b.getRepresented().movedProperty().setValue(!b.getRepresented().movedProperty().getValue());
                ////////////
                //this.getRepresented().getElements().forEach((a)->{a.});
                //TooltipUtil.install(c,this.getRepresented().getName());
                //Tooltip.
                System.out.println(("Node X:" + b.getRepresented().getCoordinates().toPoint2D().getX() + " " + "Node Y:" + b.getRepresented().getCoordinates().toPoint2D().getY()));
                System.out.println("X:" + (a.getCenterX() + a.getTranslateX()) + " " + "Y:" + (a.getCenterY() + a.getTranslateY()));
            });
        });

        this.getShape().setOnMousePressed((t) -> {
            if (!selection.contains(this) && !t.isShiftDown()) {
                clearSelection();

            }
            this.addToSelection();
        });

        this.getShape().setOnMouseDragged((t) -> {


            if (!t.isPrimaryButtonDown() || !moveable) {
                return;
            }

            if (mousePressedX == -1 && mousePressedY == -1) {
                mousePressedX = t.getX();
                mousePressedY = t.getY();
            }
            double offsetX = (t.getX() - mousePressedX);
            double offsetY = (t.getY() - mousePressedY);

            selection.forEach((b) -> {
                Circle a = b.getShape();
                //Circle c = (Circle) (t.getSource());
                //Tooltip tc = new Tooltip("("+(Math.round(c.getTranslateX()+offsetX) + "," + Math.round(c.getTranslateY()+offsetY)+ ")"));
                //Tooltip.install(c,tc);

                a.setTranslateX(a.getTranslateX() + offsetX);
                a.setTranslateY(a.getTranslateY() + offsetY);
                //System.out.println("X:"+ (c.getCenterX()+c.getTranslateX())+" "+"Y:"+ (c.getCenterY()+c.getTranslateY()));

                //this.getRepresented().movedProperty().setValue(!this.getRepresented().movedProperty().getValue());
            });

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
        else this.getShape().setCursor(Cursor.DEFAULT);
    }

    public void addToSelection() {
        this.getShape().setFill(Color.BLUE);
        selection.add(this);
    }

    public void removeFromSelection() {
        this.getShape().setFill(Color.BLACK);
        selection.remove(this);
    }

    public static void clearSelection() {
        selection.forEach((b) -> {
            b.getShape().setFill(Color.BLACK);
        });
        selection.clear();
    }
    public static void emptySelection(){
        selection.clear();
    }

    public static HashSet<Junction> getSelection() {
        return selection;
    }

    @Override
    public boolean getMoveable() {
        return moveable;
    }

}
