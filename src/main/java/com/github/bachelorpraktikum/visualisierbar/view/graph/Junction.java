package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Coordinates;
import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.moveable;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javax.annotation.Nonnull;

import javafx.scene.Cursor;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;


public final class Junction extends SingleGraphShapeBase<Node, Circle> implements com.github.bachelorpraktikum.visualisierbar.view.moveable {

    private static final double CALIBRATION_COEFFICIENT = 0.1;
    private static HashSet<Junction> selection = new HashSet<>(128);
    private static double mousePressedX = -1;
    private static double mousePressedY = -1;

    private boolean moveable;

    Junction(Node node, CoordinatesAdapter adapter) {
        super(node, adapter);

        setMoveable(false);
      /*
        adapter.OffsetXproperty().addListener((a)->{
            Point2D position = adapter.apply(node);
            this.getShape().setCenterX(position.getX());
            //this.getShape().setCenterY(position.getY());
            this.getRepresented().movedProperty().setValue(!this.getRepresented().movedProperty().getValue());

        });

        adapter.OffsetYproperty().addListener((a)->{
            Point2D position = adapter.apply(node);
            this.getShape().setCenterY(position.getY());
            //this.getShape().setCenterY(position.getY());
            this.getRepresented().movedProperty().setValue(!this.getRepresented().movedProperty().getValue());

        });

        adapter.movedProperty().addListener((t)->{
            this.getRepresented().setCoordinates(adapter.reverse(new Point2D (((int) this.getShape().getCenterX()), (int) this.getShape().getCenterY())));
            //this.getRepresented().movedProperty().setValue(!this.getRepresented().movedProperty().getValue());

        });
        */
        this.getShape().setOnMouseReleased((event) -> {
            if (!moveable) return;
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
                //b.getRepresented().setCoordinates(new Coordinates(((int) a.getCenterX()), (int) a.getCenterY()));
                b.getRepresented().setCoordinates(adapter.reverse(new Point2D(((int) a.getCenterX()), (int) a.getCenterY())));

                a.setTranslateX(0);
                a.setTranslateY(0);
                b.getRepresented().movedProperty().setValue(!b.getRepresented().movedProperty().getValue());
                ////////////
                //this.getRepresented().getElements().forEach((a)->{a.});
                //TooltipUtil.install(c,this.getRepresented().getName());
                //Tooltip.
                //System.out.println(("Node X:" + b.getRepresented().getCoordinates().toPoint2D().getX() + " " + "Node Y:" + b.getRepresented().getCoordinates().toPoint2D().getY()));
                //System.out.println("X:" + (a.getCenterX() + a.getTranslateX()) + " " + "Y:" + (a.getCenterY() + a.getTranslateY()));
                event.consume();
            });
        });

        this.getShape().setOnMousePressed((t) -> {
            if (t.isPrimaryButtonDown()) {
                if (!moveable) return;
                if (!selection.contains(this) && !t.isShiftDown()) {
                    clearSelection();
                }
                this.addToSelection();
                t.consume();
            }
            if(t.isSecondaryButtonDown()&& moveable){

                Dialog<LinkedList<String>> dialog = new Dialog<>();

                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);

                dialog.setTitle("Node Editor");
                dialog.setHeaderText(null);
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));
                TextField name = new TextField();
                name.setText(this.getRepresented().getName());
                TextField coordinates = new TextField();
                coordinates.setText("(" + this.getRepresented().getCoordinates().getX() +","+ this.getRepresented().getCoordinates().getY()+ ")");
                /*
                TextField edges =new TextField();
                edges.setEditable(false);
                String edgesText = "";
                Iterator it = this.getRepresented().getEdges().iterator();
                while (it.hasNext())
                    edgesText= edgesText + it.next().toString() + System.getProperty(System.lineSeparator());
                edges.setText(String.format(edgesText));
                //edges.setOnScroll();
                //edges.setPrefWidth(3);
                */
                //ChoiceBox type = new ChoiceBox("None", "")
                int i =0;
                LinkedList<String> c = new LinkedList<>();
                for(Element.Type type :Element.Type.values()){

                c.add(type.getName());
                }

                ChoiceBox element = new ChoiceBox();
                //element.setConverter();
                element.getItems().add("None");
                element.setValue(element.getItems().get(0));
                element.getItems().addAll(c);
                grid.add(element,2,0);

                grid.add(new Label("Name"), 0, 0);
                grid.add(name, 1, 0);
                grid.add(new Label("Coordinates:"), 0, 1);
                grid.add(coordinates, 1, 1);
                //grid.add(edges, 1, 2, 2, 5);
                dialog.getDialogPane().setContent(grid);
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.APPLY) {
                        LinkedList<String> result = new LinkedList<>();
                        //return new Pair<>(name.getText(), coordinates.getText());
                        result.add(name.getText());
                        result.add(coordinates.getText());
                        result.add(((String) element.getValue()));
                        return result;
                    }
                    return null;
                });
                Pattern p = Pattern.compile("\\(\\d+,\\d+\\)");
                Pattern d = Pattern.compile("\\d+");
                //dialog.show();
                Optional<LinkedList<String>> result = dialog.showAndWait();
                if(result.isPresent()){
                    //this.getRepresented().getContext()//result.get().getKey()
                    Matcher m = p.matcher(result.get().get(1));
                    if(m.matches()){
                        Matcher dm = d.matcher(m.group());
                        dm.find();
                        int x =Integer.parseInt(dm.group());
                        dm.find();
                        int y = Integer.parseInt(dm.group());
                        this.getRepresented().setCoordinates(new Coordinates(x,y));
                        this.getRepresented().moved();
                        relocate(this.getShape());
                    }
                    else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setX(t.getX());
                        alert.setY(t.getY());
                        alert.setGraphic(null);
                        alert.setHeaderText(null);
                        alert.setContentText("Invalid Coordinates");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }

                    if (this.getRepresented().getName() != result.get().get(0) | this.getRepresented().setName(result.get().get(0))){
                        initializedShape(this.getShape());
                    }

                    else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setX(t.getX());
                        alert.setY(t.getY());
                        alert.setGraphic(null);
                        alert.setHeaderText(null);
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }

                   //this.getRepresented().setCoordinates(result.get().getValue());
                }
                this.getRepresented().getElements().forEach((test)->{System.out.println(test);});

                t.consume();
            }
        });

        this.getShape().setOnMouseDragged((t) -> {
            if (!moveable) return;

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

    public static void emptySelection() {
        selection.clear();
    }

    public static HashSet<Junction> getSelection() {
        return selection;
    }

    @Override
    public boolean getMoveable() {
        return moveable;
    }

    public void updateShapePosition(){

    }

}
