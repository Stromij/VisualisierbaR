package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.*;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javax.annotation.Nonnull;
import javafx.beans.value.*;
import javafx.scene.Cursor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class Junction extends SingleGraphShapeBase<Node, Circle> implements com.github.bachelorpraktikum.visualisierbar.view.moveable {

    private static DoubleProperty CALIBRATION_COEFFICIENT= new SimpleDoubleProperty(0.1d);
    private static HashSet<Junction> selection = new HashSet<>(128);
    private static double mousePressedX = -1;
    private static double mousePressedY = -1;
    private static Logger logger = Logger.getLogger(Junction.class.getName());
    private boolean moveable;
    private List<ChangeListener> listeners= new ArrayList<>(1);
    private Tooltip tooltip;
    private int i=7;                                                                                                        //for grid layout


    Junction(Node node, CoordinatesAdapter adapter) {
        super(node, adapter);

        setMoveable(false);

        this.getShape().setOnMouseReleased((event) -> {
            if (!moveable) return;

            mousePressedX = -1;
            mousePressedY = -1;
            selection.forEach((b) -> {
                Circle a = b.getShape();
                a.setTranslateX(Math.round(a.getTranslateX()));                                                             //snap to valid coordinates
                a.setTranslateY(Math.round(a.getTranslateY()));                                                             //technically you would want to move the full Node here
                double xx = a.getCenterX() + a.getTranslateX();                                                             //but we move only the circle and let the rest adjust itself through
                double yy = a.getCenterY() + a.getTranslateY();                                                             //listeners
                if (xx < 0 || yy < 0) {
                    System.out.println(a.getTranslateY());
                    Map<Node, GraphShape<Node> > nodes= this.getRepresented().getGraph().getNodes();
                    if(xx >= 0 && yy<0 ){                                                                                   //whole bunch of code for handling negative coordinates that currently
                       yy= 0 - yy;                                                                                          //does not work
                        for(Node n : nodes.keySet()){
                            if(n != b.getRepresented() ){
                                n.setCoordinates(adapter.reverse(new Point2D(n.getCoordinates().getX(),(int)(n.getCoordinates().getY()+yy))));
                                n.moved();
                            }
                        }
                        yy=0;

                    }
                    else if(xx<0 && yy>= 0) {
                        xx = 0 - xx;
                        for (Node n : nodes.keySet()) {
                            if (n != b.getRepresented()) {
                                n.setCoordinates(adapter.reverse(new Point2D((int)( n.getCoordinates().getX() + xx), n.getCoordinates().getY())));
                                n.moved();
                            }
                        }
                        xx=0;
                    }
                   else{
                        xx= 0 - xx;
                        yy= 0 - yy;
                        for (Node n : nodes.keySet()) {
                            if (n != b.getRepresented()) {
                                n.setCoordinates(adapter.reverse(new Point2D((int)( n.getCoordinates().getX() + xx), (int)( n.getCoordinates().getY()+yy))));
                                n.moved();
                            }
                        }
                        xx=0;
                        yy=0;
                    }
                    /*System.out.println("Coordiantes invalid");
                    a.setTranslateY(0);
                    a.setTranslateX(0);
                    return;*/
                }
                a.setCenterX(xx);
                a.setCenterY(yy);
                b.getRepresented().setCoordinates(adapter.reverse(new Point2D(((int) a.getCenterX()), (int) a.getCenterY())));  //update coordinates in the model

                a.setTranslateX(0);
                a.setTranslateY(0);
                b.getRepresented().moved();                                                                                 //indicate that the node has moved to notify edges and elements

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

                Alert alert = new Alert(Alert.AlertType.ERROR);                                                             //prepare Error dialog for later use
                alert.setTitle("Error");
                alert.setGraphic(null);
                alert.setHeaderText(null);
                tooltip.hide();

                Dialog<LinkedList<String>> dialog = new Dialog<>();                                                         //prepare Dialog Pane
                dialog.setResizable(true);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
                ((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
                dialog.setTitle("Node Editor");
                dialog.setHeaderText(null);
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(5);
                grid.setPadding(new Insets(20, 150, 10, 10));
                HashMap<Element, TextField> ElementNameTextFields= new HashMap<>(3);                            //Holds the TextFields of the Elements of the Node


                TextField name = new TextField();                                                                          //TextField for the Node name
                name.setText(this.getRepresented().getName());

                TextField coordinates = new TextField();
                coordinates.setText("(" + this.getRepresented().getCoordinates().getX() +","+ this.getRepresented().getCoordinates().getY()+ ")");

                LinkedList<String> c = new LinkedList<>();
                for(Element.Type type :Element.Type.values()){
                    c.add(type.getLogName());
                }
                ChoiceBox<String> element = new ChoiceBox<>();                                                              //element Type choice box
                element.getItems().addAll(c);
                element.setValue(element.getItems().get(0));
                c.clear();

                for(Element.State state :Element.State.values()){
                    c.add(state.name());
                }
                ChoiceBox<String> sig = new ChoiceBox<>();                                                                  //signal choice box
                sig.getItems().addAll(c);
                sig.setValue(sig.getItems().get(0));

                grid.add(new Label("Name"), 0, 0);                                                  //Labels for UI
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
                createButton.setOnAction((tt)->{                                                                            //add Element procedure
                    String newName = Ename.getText();

                    //something went horribly wrong and we abandon ship
                    if (this.getRepresented().getGraph()==null){                                                            //this shouldnt happen if you dont mess up the Graph procedures
                        alert.setContentText("Internal Error! Check Log.");
                        alert.showAndWait();
                        tt.consume();
                        logger.log(Level.SEVERE, this.getRepresented().getName()+ "attached Graph is null");
                        return;
                    }
                    Element newElement;
                    if(!Element.in(this.getRepresented().getGraph().getContext()).NameExists(newName)){
                        Element.Type eType = Element.Type.fromName(element.getValue());
                        if (eType==Element.Type.WeichenPunkt){                                                              //Weichen are treated seperately
                            Node node1=null;
                            Node node2=null;                                                                                //checking wether Input by the user is valid
                            if(this.getRepresented().getEdges().size()!=3 || !selection.contains(this)){                    //meaning 3 correctly connected Nodes are selected
                                alert.setContentText("Main Point needs to have 3 edges, this one has "+ this.getRepresented().getEdges().size());
                                alert.showAndWait();
                                tt.consume();
                                return;
                            }

                            if(getSelection().size()!= 3){
                                alert.setContentText("Select 3 properly connected Nodes");
                                alert.showAndWait();
                                tt.consume();
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
                                tt.consume();
                                return;
                            }
                            String name1=null;
                            String name2=null;

                            //something went horribly wrong and we abandon ship
                            if(node1==null|| node2==null || node1.getGraph()==null || node2.getGraph()==null){
                                alert.setContentText("Internal Error! Check Log.");
                                alert.showAndWait();
                                tt.consume();
                                logger.log(Level.SEVERE, "some Nodes in this Junction have no Graph attached")
                                ;return;}

                            RandomString gen = new RandomString(4, ThreadLocalRandom.current());                        //generate random names for the other 2 elements
                            for (int i=0; i<10000; i++){                                                                      //
                                if(i==9999) throw new IllegalStateException("Namespace for new Elements is not large enough");
                                 name1= gen.nextString();
                                 name2= gen.nextString();
                                 if(name1.equals(name2)) continue;
                                 if(!Element.in(node1.getGraph().getContext()).NameExists(newName+name1) && !Element.in(node2.getGraph().getContext()).NameExists(newName+name2)) break;
                            }
                            newElement=Element.in(this.getRepresented().getGraph().getContext()).create(newName, eType, this.getRepresented(), Element.State.fromName(sig.getValue()));
                            Element e2=Element.in(node1.getGraph().getContext()).create(newName+name1, eType, node1, Element.State.fromName(sig.getValue()));
                            Element e3=Element.in(node2.getGraph().getContext()).create(newName+name2, eType, node2, Element.State.fromName(sig.getValue()));
                            this.getRepresented().getGraph().addElement(newElement);
                            node1.getGraph().addElement(e2);
                            node2.getGraph().addElement(e3);
                        }
                        else{
                            newElement=Element.in(this.getRepresented().getGraph().getContext()).create(newName, eType, this.getRepresented(), Element.State.fromName(sig.getValue()));
                            this.getRepresented().getGraph().addElement(newElement);                                            //normal elements are simply added
                        }                                                                                                       //end of special treatment for Weichen

                        TextField eName= new TextField(newElement.getName());                                                   //Add UI for the editing of the new Element
                        ElementNameTextFields.put(newElement, eName);
                        Label type = new Label(newElement.getType().getName());

                        ChoiceBox<String> direction = new ChoiceBox<>();                                                        //direction choice box
                        direction.setOnAction(directionEvent->{                                                                 //when Direction changes redraw element to indicate direction

                            String NodeName = direction.getValue();
                            Node otherNode = Node.in(this.getRepresented().getGraph().getContext()).get(NodeName);
                            newElement.setDirection(otherNode);
                            this.getRepresented().getGraph().rebuildComposite(this.getRepresented());

                        });
                        for(Edge e : this.getRepresented().getEdges()){
                            Node otherNode = e.getOtherNode(this.getRepresented());
                            direction.getItems().addAll(otherNode.getName());                                                   //set direction choice box Items
                        }

                        ChoiceBox<String> logicalGroup = new ChoiceBox<>();                                                     //logical Group choice box
                        logicalGroup.getItems().add("new Group");                                                               //0 is newGroup
                        logicalGroup.getItems().add("no Group");                                                                        //1 is no Group
                        com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.getAll().forEach((a)->{     //add all existing groups as options
                            logicalGroup.getItems().add(a.getName());
                        });
                        if(newElement.getLogicalGroup()==null) logicalGroup.getSelectionModel().select(1);                 //select no Group as default
                        else
                            logicalGroup.getSelectionModel().select(newElement.getLogicalGroup().getName());

                        logicalGroup.setOnAction(lgEvent->{
                            if(logicalGroup.getSelectionModel().getSelectedIndex()==0){
                                Dialog<String> groupCreationDialog = new Dialog<>();                                            //Dialog for new Group creation set up

                                groupCreationDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
                                ((Button) groupCreationDialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
                                groupCreationDialog.setTitle("Create new Group");
                                groupCreationDialog.setHeaderText(null);
                                TextField GroupCreationName = new TextField();
                                groupCreationDialog.getDialogPane().setContent(GroupCreationName);
                                groupCreationDialog.setResultConverter((dialogButton)->{
                                    if(dialogButton == ButtonType.APPLY){
                                        return GroupCreationName.getText();
                                    }
                                    else
                                        return null;
                                });

                                Optional<String> result = groupCreationDialog.showAndWait();                                    // Actual Code for Creation of a new Group
                                if(result.isPresent()){
                                    if(com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.NameExists(result.get())){
                                        alert.setContentText("Group already exists");
                                        alert.showAndWait();
                                        if(newElement.getLogicalGroup()==null) logicalGroup.getSelectionModel().select(1);      //select noGroup string because element is in no group since name is taken
                                        else
                                            logicalGroup.setValue(newElement.getLogicalGroup().getName());

                                    }
                                    else {
                                        LogicalGroup newGroup = com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.create(result.get(), "????");   //TODO Kind
                                        if (newElement.getLogicalGroup()!=null){
                                        newElement.getLogicalGroup().removeElement(newElement);
                                        }
                                        newGroup.addElement(newElement);
                                        newElement.setLogicalGroup(newGroup);
                                        logicalGroup.getItems().add(newGroup.getName());
                                        logicalGroup.getSelectionModel().select(newGroup.getName());
                                    }
                                }
                                else{
                                    if(newElement.getLogicalGroup()==null) logicalGroup.getSelectionModel().select(1);
                                    else
                                        logicalGroup.getSelectionModel().select(newElement.getLogicalGroup().getName());
                                }
                            }
                            else{
                                if (newElement.getLogicalGroup()!=null){                                                       //code for switching Group
                                    newElement.getLogicalGroup().removeElement(newElement);
                                }
                                newElement.setLogicalGroup(com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.get(logicalGroup.getValue()));
                            }
                        });


                        grid.add(eName,1,i);                                                                        //Editor layout code for new Elements
                        grid.add(type,2,i);
                        grid.add(logicalGroup, 3, i);
                        if(newElement.getType().isComposite())grid.add(direction,4,i);
                        Button deleteButton = new Button();
                        deleteButton.setText("X");
                        deleteButton.setTextFill(Color.RED);
                        deleteButton.setOnAction((event)->{
                            if (newElement.getGraph()==null) return;                                                        //should never happen
                            newElement.getGraph().removeElement(newElement);
                            grid.getChildren().remove(eName);
                            ElementNameTextFields.remove(newElement);
                            grid.getChildren().remove(type);
                            grid.getChildren().remove(deleteButton);
                            grid.getChildren().remove(direction);
                            grid.getChildren().remove(logicalGroup);


                        });
                        grid.add(deleteButton,5,i);
                        i++;
                        dialog.getDialogPane().getScene().getWindow().sizeToScene();

                    }
                    else{
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        tt.consume();

                    }
                });                                                                                                         //end for Elemente creation Code
                grid.add(createButton,1,6);

                for(Element elements : this.getRepresented().getElements()){                                                //Code for Elements that are already there
                    TextField eName= new TextField(elements.getName());                                                     //quite a bit of code duplication, you could probably change this to be
                    ElementNameTextFields.put(elements, eName);                                                             //much more elegant
                    Label type = new Label(elements.getType().getName());
                    ChoiceBox<String> direction = new ChoiceBox<>();      //direction Type choice box
                    ChoiceBox<String> logicalGroup = new ChoiceBox<>();
                    direction.setOnAction(directionEvent->{
                        String NodeName = direction.getValue();
                        if(this.getRepresented().getGraph()!=null) {                                                        //should never be null
                            Node otherNode = Node.in(this.getRepresented().getGraph().getContext()).get(NodeName);
                            elements.setDirection(otherNode);
                            this.getRepresented().getGraph().rebuildComposite(this.getRepresented());
                        }

                    });
                    for(Edge e : this.getRepresented().getEdges()){
                        Node otherNode = e.getOtherNode(this.getRepresented());
                        direction.getItems().addAll(otherNode.getName());
                        if(otherNode==elements.getDirection()) direction.setValue(otherNode.getName());
                    }

                    logicalGroup.getItems().add("new Group");
                    logicalGroup.getItems().add("no Group");
                    com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.getAll().forEach((a)-> logicalGroup.getItems().add(a.getName()));
                    if(elements.getLogicalGroup()==null) logicalGroup.getSelectionModel().select(1);
                    else
                        logicalGroup.getSelectionModel().select(elements.getLogicalGroup().getName());

                    logicalGroup.setOnAction(lgEvent->{
                        if(logicalGroup.getSelectionModel().getSelectedIndex()==0){
                            Dialog<String> groupCreationDialog = new Dialog<>();

                            groupCreationDialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
                            ((Button) groupCreationDialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);
                            groupCreationDialog.setTitle("Create new Group");
                            groupCreationDialog.setHeaderText(null);
                            TextField GroupCreationName = new TextField();
                            groupCreationDialog.getDialogPane().setContent(GroupCreationName);
                            groupCreationDialog.setResultConverter((dialogButton)->{
                                if(dialogButton == ButtonType.APPLY){
                                    return GroupCreationName.getText();
                                }
                                else
                                    return null;
                            });

                            Optional<String> result = groupCreationDialog.showAndWait();
                            if(result.isPresent()){
                                if(com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.NameExists(result.get())){
                                    alert.setContentText("Group already exists");
                                    alert.showAndWait();
                                    if(elements.getLogicalGroup()==null) logicalGroup.getSelectionModel().select(1);      //select empty string because element is in no group
                                    else
                                        logicalGroup.setValue(elements.getLogicalGroup().getName());
                                }
                                else {
                                    LogicalGroup newGroup = com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.create(result.get(), "????");
                                    if (elements.getLogicalGroup()!=null){
                                        elements.getLogicalGroup().removeElement(elements);
                                    }
                                    newGroup.addElement(elements);
                                    elements.setLogicalGroup(newGroup);
                                    logicalGroup.getItems().add(newGroup.getName());
                                    logicalGroup.getSelectionModel().select(newGroup.getName());
                                }
                            }
                            else{
                                if(elements.getLogicalGroup()==null) logicalGroup.getSelectionModel().select(1);
                                else
                                    logicalGroup.getSelectionModel().select(elements.getLogicalGroup().getName());
                            }
                        }
                        else{
                            if (elements.getLogicalGroup()!=null){
                                elements.getLogicalGroup().removeElement(elements);
                            }
                            elements.setLogicalGroup(com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup.GroupFactory.get(logicalGroup.getValue()));
                        }
                    });





                    grid.add(eName,1,i);
                    grid.add(type,2,i);
                    grid.add(logicalGroup,3, i );
                    if(elements.getType().isComposite())grid.add(direction,4,i);
                    Button deleteButton = new Button();
                    deleteButton.setText("X");
                    deleteButton.setTextFill(Color.RED);
                    deleteButton.setOnAction((tt)->{
                        if (elements.getGraph()==null) { System.out.println("Error element not in Graph");return;}
                        elements.getGraph().removeElement(elements);
                        grid.getChildren().remove(eName);
                        ElementNameTextFields.remove(elements);
                        grid.getChildren().remove(type);
                        grid.getChildren().remove(deleteButton);
                        grid.getChildren().remove(direction);
                        grid.getChildren().remove(logicalGroup);

                    });
                    grid.add(deleteButton,5,i);
                    i++;

                }                                                                                                           //end of duplicate Code

                dialog.getDialogPane().setContent(grid);
                dialog.setResultConverter(dialogButton -> {                                                                 //get user Input for the entire Dialog
                    if (dialogButton == ButtonType.APPLY) {
                        LinkedList<String> result = new LinkedList<>();
                        result.add(name.getText());
                        result.add(coordinates.getText());
                        ElementNameTextFields.forEach((a,b)->{

                        });
                        return result;
                    }
                    return null;
                });
                Pattern p = Pattern.compile("\\(\\d+,\\d+\\)");
                Pattern d = Pattern.compile("\\d+");
                Optional<LinkedList<String>> result = dialog.showAndWait();
                if(result.isPresent()){
                    Matcher m = p.matcher(result.get().get(1));                                                             //check if input for coordinates matches (x,y) format
                    if(m.matches()){
                        Matcher dm = d.matcher(m.group());
                        dm.find();
                        int x =Integer.parseInt(dm.group());
                        dm.find();
                        int y = Integer.parseInt(dm.group());
                        this.getRepresented().setCoordinates(new Coordinates(x,y));                                        //set new Coordinates
                        this.getRepresented().moved();
                        relocate(this.getShape());
                    }
                    else {
                        alert.setContentText("Invalid Coordinates");
                        alert.showAndWait();
                        return;
                    }

                    if (this.getRepresented().getName().equals(result.get().get(0)) | this.getRepresented().setName(result.get().get(0))){      //set new Name
                        initializedShape(this.getShape());
                    }

                    else{
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        return;
                    }

                    ElementNameTextFields.forEach((a,b)->{                                                                          //set Element names
                        if(a.getGraph()!=null){
                            if(a.getName().equals(b.getText()) | a.setName(b.getText())){                                           //TODO Tooltip updates for Elements
                                //initializedShape(a.getGraph().getElements().get(a));
                                //TooltipUtil.install(a.getGraph().getElements().get(a), new Tooltip(a.getName()));
                            }
                            else {
                                alert.setContentText("Name already taken");
                                alert.showAndWait();
                            }
                        }
                        else{
                            alert.setContentText("Internal Error Element not in Graph");
                            alert.showAndWait();
                        }
                    });

                }
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
    }




    @Override
    protected void initializedShape(Circle circle) {
        String tooltipName = getRepresented().getName();
        if(getRepresented().getAbsName() != null)
            {tooltipName = tooltipName.concat(" | ");
                tooltipName = tooltipName.concat(getRepresented().getAbsName());
            }
        tooltip = new Tooltip(tooltipName);
        TooltipUtil.install(circle, tooltip);
    }

    @Nonnull
    @Override
    public Circle createShape() {

        ChangeListener sizeListener = ((observable, oldValue, newValue) -> resize(this.getShape()));
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

    /**
     * Sets wether the Node can be moved in the Editor
     *
     * @param moveable  true if the Node should be able to be moved
     */
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
