package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.*;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.moveable;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javax.annotation.Nonnull;

import javafx.scene.Cursor;
import javafx.scene.shape.Shape;
import javafx.util.Pair;

import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
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
    //public Shape elements;
    //private DropShadow highlightGlow;

    Junction(Node node, CoordinatesAdapter adapter) {
        super(node, adapter);
        /*
        highlightGlow = new DropShadow();
        highlightGlow.setOffsetY(0f);
        highlightGlow.setOffsetX(0f);
        highlightGlow.setColor(Color.BLUE);
        //highlightGlow.setRadius(getCalibrationBase() * CALIBRATION_COEFFICIENT * 1.5);
        */

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

                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setGraphic(null);
                alert.setHeaderText(null);

                Dialog<LinkedList<String>> dialog = new Dialog<>();

                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);

                dialog.setTitle("Node Editor");
                dialog.setHeaderText(null);
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(5);
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
                LinkedList<String> c = new LinkedList<>();
                for(Element.Type type :Element.Type.values()){
                    c.add(type.getLogName()); //////////TEST///////////////////
                }
                ChoiceBox<String> element = new ChoiceBox<>();
                element.getItems().addAll(c);
                element.setValue(element.getItems().get(0));

                c.clear();

                for(Element.State state :Element.State.values()){
                    c.add(state.name());
                }
                ChoiceBox<String> sig = new ChoiceBox<>();
                sig.getItems().addAll(c);
                sig.setValue(sig.getItems().get(0));

                grid.add(new Label("Name"), 0, 0);
                grid.add(name, 1, 0);
                grid.add(new Label("Coordinates:"), 0, 1);
                grid.add(coordinates, 1, 1);

                Separator  sep = new Separator();
                sep.setOrientation(Orientation.HORIZONTAL);
                grid.add(sep,0,2);

                grid.add(new Label("Create new Element"),0,3);
                TextField Ename = new TextField("Name");
                grid.add(Ename,1,3);
                grid.add(element,1,4);
                grid.add(new Label("Type"),0, 4 );
                grid.add(new Label("Signal"),0,5);
                grid.add(sig, 1, 5);
                Button createButton = new Button();
                createButton.setText("create");
                createButton.setOnAction((tt)->{
                    String newName = Ename.getText();

                    //TODO Log something went wrong
                    //something went horribly wrong and we abandon ship
                    if (this.getRepresented().getGraph()==null){return;}
                    if(!Element.in(this.getRepresented().getGraph().getContext()).NameExists(newName)){
                        Element.Type eType = Element.Type.fromName(element.getValue());
                        if (eType==Element.Type.WeichenPunkt){

                            Node node1=null;
                            Node node2=null;
                            if(this.getRepresented().getEdges().size()!=3){
                                alert.setContentText("Main Point needs to have 3 edges, this one has "+ this.getRepresented().getEdges().size());
                                alert.showAndWait();
                                t.consume();
                                return;
                            }

                            if(getSelection().size()!= 3){
                                alert.setContentText("Select 3 properly connected Nodes");
                                alert.showAndWait();
                                t.consume();
                                return;
                            }
                            boolean error = true;
                            for(Junction j : getSelection()){
                                if (j != this){
                                    for (Edge e : j.getRepresented().getEdges()){
                                        if (node1 != j.getRepresented()) node1=j.getRepresented();
                                        else{
                                            if (node2 == null) node2=j.getRepresented();}
                                        if(e.getOtherNode(j.getRepresented())==this.getRepresented()) error=false;
                                    }
                                    if (error) break;
                                }
                            }
                            if (error){
                                alert.setContentText("Nodes not properly connected");
                                alert.showAndWait();
                                t.consume();
                                return;
                            }
                            String name1=null;
                            String name2=null;
                            //something went horribly wrong and we abandon ship
                            if(node1==null|| node2==null || node1.getGraph()==null || node2.getGraph()==null){return;}

                            RandomString gen = new RandomString(4, ThreadLocalRandom.current());
                            for (int i=0; i<10000; i++){
                                 name1= gen.nextString();
                                 name2= gen.nextString();
                                 if(name1.equals(name2)) continue;
                                 if(!Element.in(node1.getGraph().getContext()).NameExists(newName+name1) && !Element.in(node2.getGraph().getContext()).NameExists(newName+name2)) break;
                            }
                            Element e1=Element.in(this.getRepresented().getGraph().getContext()).create(newName, eType, this.getRepresented(), Element.State.fromName(sig.getValue()));
                            Element e2=Element.in(node1.getGraph().getContext()).create(newName+name1, eType, node1, Element.State.fromName(sig.getValue()));
                            Element e3=Element.in(node2.getGraph().getContext()).create(newName+name2, eType, node2, Element.State.fromName(sig.getValue()));
                            this.getRepresented().getGraph().addElement(e1);
                            node1.getGraph().addElement(e2);
                            node2.getGraph().addElement(e3);
                        }
                        else{
                            this.getRepresented().getGraph().addElement(Element.in(this.getRepresented().getGraph().getContext()).create(newName, eType, this.getRepresented(), Element.State.fromName(sig.getValue())));
                        }
                        /*
                        if (eType==Element.Type.WeichenPunkt){

                            Dialog<LinkedList<String>> WPdialog = new Dialog<>();

                            WPdialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);

                            WPdialog.setTitle("Node Editor");
                            WPdialog.setHeaderText(null);
                            GridPane WPgrid = new GridPane();
                            WPgrid.setHgap(10);
                            WPgrid.setVgap(5);
                            WPgrid.setPadding(new Insets(20, 150, 10, 10));
                            Button [] a;
                            a[0]=new Button();
                        }
                        */
                    }
                    else{
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }


                });
                grid.add(createButton,1,6);
                //grid.add(edges, 1, 2, 2, 5);
                dialog.getDialogPane().setContent(grid);
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == ButtonType.APPLY) {
                        LinkedList<String> result = new LinkedList<>();
                        //return new Pair<>(name.getText(), coordinates.getText());
                        result.add(name.getText());
                        result.add(coordinates.getText());
                        //result.add(((String) element.getValue()));
                        //result.add((String) sig.getValue());
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
                        alert.setContentText("Invalid Coordinates");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }

                    if (this.getRepresented().getName().equals(result.get().get(0)) | this.getRepresented().setName(result.get().get(0))){
                        initializedShape(this.getShape());
                    }

                    else{
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }

                   //this.getRepresented().setCoordinates(result.get().getValue());

                }
                /////DEBUG////
                this.getRepresented().getElements().forEach((test)->{System.out.println(test+ " " + test.getType());});

                t.consume();
            }
        });

        this.getShape().setOnMouseDragged((t) -> {
            if (!moveable) return;

            if (!t.isPrimaryButtonDown()) {
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
        //highlightGlow.setRadius(getCalibrationBase() * CALIBRATION_COEFFICIENT * 1.5);
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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }


    public void setMoveable(boolean moveable) {
        this.moveable = moveable;
        if (moveable == true) this.getShape().setCursor(Cursor.HAND);
        else this.getShape().setCursor(Cursor.DEFAULT);
    }

    /**
     * Adds this Junction to the Selection and highlights the Shape with blue
     */
    public void addToSelection() {
        this.getShape().setFill(Color.BLUE);
        //this.getShape().setEffect(highlightGlow);
        selection.add(this);
    }
    /**
     * Removes this Junction from the Selection and sets the Color to standard black
     */

    public void removeFromSelection() {
        this.getShape().setFill(Color.BLACK);
        //this.getShape().setEffect(null);
        selection.remove(this);
    }

    /**
     * empties the Selection properly
     */

    public static void clearSelection() {
        selection.forEach((b) -> {
            b.getShape().setFill(Color.BLACK);
            //b.getShape().setEffect(null);
        });
        selection.clear();
    }

    /**
     * empties the selection without cleaning up, use with care
     */
    public static void emptySelection() {
        selection.clear();
    }

    /**
     * Returns the current selection
     * @return the static Selection variable shared among ALL Junctions
     */
    public static HashSet<Junction> getSelection() {
        return selection;
    }

    @Override
    public boolean getMoveable() {
        return moveable;
    }



}
