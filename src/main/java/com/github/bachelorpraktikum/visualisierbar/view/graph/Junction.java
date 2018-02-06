package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.*;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.moveable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javax.annotation.Nonnull;
import javafx.beans.value.*;

import javafx.scene.Cursor;
import javafx.scene.shape.Shape;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Junction extends SingleGraphShapeBase<Node, Circle> implements com.github.bachelorpraktikum.visualisierbar.view.moveable {

    private static DoubleProperty CALIBRATION_COEFFICIENT= new SimpleDoubleProperty(0.1d);
    private static HashSet<Junction> selection = new HashSet<>(128);
    private static double mousePressedX = -1;
    private static double mousePressedY = -1;
    private boolean moveable;
    private List<ChangeListener> listeners= new ArrayList<>(1);
    private Tooltip tooltip;
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
                a.setTranslateX(Math.round(a.getTranslateX()));     //snap to valid coordinates
                a.setTranslateY(Math.round(a.getTranslateY()));
                double xx = a.getCenterX() + a.getTranslateX();
                double yy = a.getCenterY() + a.getTranslateY();
                if (xx < 0 || yy < 0) {
                    Map<Node, GraphShape<Node> > nodes= this.getRepresented().getGraph().getNodes();
                    if(xx >= 0 && yy<0 ){
                       yy= 0 - yy;
                        for(Node n : nodes.keySet()){
                            if(n != b.getRepresented() ){
                                n.setCoordinates(adapter.reverse(new Point2D(((int) n.getCoordinates().getX()),(  (int) n.getCoordinates().getY()+yy))));
                            }
                        }

                    }
                    else if(xx<0 && yy>= 0) {
                        xx = 0 - xx;
                        for (Node n : nodes.keySet()) {
                            if (n != b.getRepresented()) {
                                n.setCoordinates(adapter.reverse(new Point2D(((int) n.getCoordinates().getX() + xx), ((int) n.getCoordinates().getY()))));
                            }
                        }
                    }
                   else{
                        xx= 0 - xx;
                        yy= 0 - yy;
                        for (Node n : nodes.keySet()) {
                            if (n != b.getRepresented()) {
                                n.setCoordinates(adapter.reverse(new Point2D(((int) n.getCoordinates().getX() + xx), ((int) n.getCoordinates().getY()+yy))));
                            }
                        }
                    }
                    /*System.out.println("Coordiantes invalid");
                    a.setTranslateY(0);
                    a.setTranslateX(0);
                    return;*/
                }
                a.setCenterX(a.getCenterX() + a.getTranslateX());
                a.setCenterY(a.getCenterY() + a.getTranslateY());
                b.getRepresented().setCoordinates(adapter.reverse(new Point2D(((int) a.getCenterX()), (int) a.getCenterY())));  //update coordinates in the model
                //System.out.println(b.getRepresented().getCoordinates());
                a.setTranslateX(0);
                a.setTranslateY(0);
                b.getRepresented().moved();                   //indicate that the node has moved to notify edges and elements

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

                Alert alert = new Alert(Alert.AlertType.ERROR);     //prepare Error dialog for later use
                alert.setTitle("Error");
                alert.setGraphic(null);
                alert.setHeaderText(null);

                Dialog<LinkedList<String>> dialog = new Dialog<>();
                tooltip.hide();
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

                LinkedList<String> c = new LinkedList<>();
                for(Element.Type type :Element.Type.values()){
                    c.add(type.getLogName());
                }
                ChoiceBox<String> element = new ChoiceBox<>();      //element Type choice box
                element.getItems().addAll(c);
                element.setValue(element.getItems().get(0));
                c.clear();

                for(Element.State state :Element.State.values()){
                    c.add(state.name());
                }
                ChoiceBox<String> sig = new ChoiceBox<>();              //signal choice box
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
                createButton.setOnAction((tt)->{                    //add Element procedure
                    String newName = Ename.getText();

                    //TODO Log something went wrong
                    //something went horribly wrong and we abandon ship
                    if (this.getRepresented().getGraph()==null){return;}
                    if(!Element.in(this.getRepresented().getGraph().getContext()).NameExists(newName)){
                        Element.Type eType = Element.Type.fromName(element.getValue());
                        if (eType==Element.Type.WeichenPunkt){                                  //Weichen are treated seperately
                            Node node1=null;
                            Node node2=null;
                            if(this.getRepresented().getEdges().size()!=3 || !selection.contains(this)){
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
                            //TODO log error
                            //something went horribly wrong and we abandon ship
                            if(node1==null|| node2==null || node1.getGraph()==null || node2.getGraph()==null){return;}

                            RandomString gen = new RandomString(4, ThreadLocalRandom.current()); //generate random names for the other 2 elements
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
                            this.getRepresented().getGraph().addElement(Element.in(this.getRepresented().getGraph().getContext()).create(newName, eType, this.getRepresented(), Element.State.fromName(sig.getValue())));       //normal elements are simply added
                        }
                    }
                    else{
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }


                });
                grid.add(createButton,1,6);
                dialog.getDialogPane().setContent(grid);
                dialog.setResultConverter(dialogButton -> {             //get user Input
                    if (dialogButton == ButtonType.APPLY) {
                        LinkedList<String> result = new LinkedList<>();
                        result.add(name.getText());
                        result.add(coordinates.getText());
                        return result;
                    }
                    return null;
                });
                Pattern p = Pattern.compile("\\(\\d+,\\d+\\)");
                Pattern d = Pattern.compile("\\d+");
                //dialog.show();
                Optional<LinkedList<String>> result = dialog.showAndWait();
                if(result.isPresent()){
                    Matcher m = p.matcher(result.get().get(1));     //check if input for coordinates matches (x,y) format
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

                    if (this.getRepresented().getName().equals(result.get().get(0)) | this.getRepresented().setName(result.get().get(0))){      //set new Name
                        initializedShape(this.getShape());
                    }

                    else{
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }

                }
                /////DEBUG////
                //this.getRepresented().getElements().forEach((test)->{System.out.println(test+ " " + test.getType());});
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
                a.setTranslateX(a.getTranslateX() + offsetX);
                a.setTranslateY(a.getTranslateY() + offsetY);
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
        double radius = getCalibrationBase() * CALIBRATION_COEFFICIENT.getValue();
        shape.setRadius(radius);
        //highlightGlow.setRadius(getCalibrationBase() * CALIBRATION_COEFFICIENT * 1.5);
    }




    @Override
    protected void initializedShape(Circle circle) {
        tooltip = new Tooltip(getRepresented().getName());
        TooltipUtil.install(circle, tooltip);
    }

    @Nonnull
    @Override
    public Circle createShape() {

        ChangeListener sizeListener = ((observable, oldValue, newValue) -> {
            resize(this.getShape());
        });
        listeners.add(sizeListener);
        CALIBRATION_COEFFICIENT.addListener(new WeakChangeListener<>(sizeListener));

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
        if (moveable) this.getShape().setCursor(Cursor.HAND);
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

    public static void setCalibrationCoefficient(double c){
        CALIBRATION_COEFFICIENT.setValue(c);
    }
    public static DoubleProperty getCALIBRATION_COEFFICIENT_prop(){
        return CALIBRATION_COEFFICIENT;
    }

    @Override
    public boolean getMoveable() {
        return moveable;
    }



}
