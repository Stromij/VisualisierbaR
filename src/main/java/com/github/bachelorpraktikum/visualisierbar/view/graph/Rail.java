package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Edge;
import com.github.bachelorpraktikum.visualisierbar.model.Shapeable;
import com.github.bachelorpraktikum.visualisierbar.view.MainController;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

import javax.annotation.Nonnull;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Rail extends SingleGraphShapeBase<Edge, Line> {

    private static final double CALIBRATION_COEFFICIENT = 0.05;
    private final List<ChangeListener> listeners;
    private Tooltip tooltip;

    Rail(Edge edge, CoordinatesAdapter adapter) {
        super(edge, adapter);
        listeners = new ArrayList<>(2);
        /*this.getShape().setOnMousePressed((t)->{

            if(t.isSecondaryButtonDown()){

                Dialog<LinkedList<String>> dialog = new Dialog<>();
                tooltip.hide();

                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
                //dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
                ((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);

                dialog.setTitle("Edge Editor");
                dialog.setHeaderText(null);
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(5);
                grid.setPadding(new Insets(20, 150, 10, 10));

                TextField name = new TextField();
                name.setText(this.getRepresented().higherName());

                TextField length  = new TextField();
                length.setText(Integer.toString(this.getRepresented().getLength()));

                grid.add(new Label("Name"), 0, 0);
                grid.add(name, 1, 0);
                grid.add(new Label("Length:"), 0, 1);
                grid.add(length, 1, 1);
                grid.add(new Label ("Node 1:"), 2, 0);
                grid.add(new Label ("Node 2:"), 2, 1);
                grid.add(new Label (this.getRepresented().getNode1().higherName()), 3,0);
                grid.add(new Label (this.getRepresented().getNode2().higherName()), 3,1);

                dialog.getDialogPane().setContent(grid);
                dialog.setResultConverter(dialogButton -> {                                //get User input
                    if (dialogButton == ButtonType.APPLY) {
                        LinkedList<String> result = new LinkedList<>();
                        //return new Pair<>(name.getText(), coordinates.getText());
                        result.add(name.getText());
                        result.add(length.getText());
                        return result;
                    }
                    return null;
                });

                Optional<LinkedList<String>> result = dialog.showAndWait();
                if(result.isPresent()){
                    if (this.getRepresented().getName() != result.get().get(0) | this.getRepresented().setName(result.get().get(0))){

                    }
                    else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setGraphic(null);
                        alert.setHeaderText(null);
                        alert.setContentText("Name already taken");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }
                    Pattern d = Pattern.compile("\\d+");
                    Matcher dm = d.matcher(result.get().get(1));
                    if (dm.matches())
                    {
                        int x =Integer.parseInt(dm.group());
                        this.getRepresented().setLength(x);
                    }

                    else{
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setGraphic(null);
                        alert.setHeaderText(null);
                        alert.setContentText("Invalid length");
                        alert.showAndWait();
                        t.consume();
                        return;
                    }
                    initializedShape(this.getShape());
                    t.consume();

                }
            }
        });*/
    }


    public void mouseListener(MouseEvent t, MainController parent){
        ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");


        if(t.isSecondaryButtonDown()){

            // Button zum Anzeigen des Elements im ABS-Code
            URL imageUrl = Junction.class.getResource("Search.fxml");

            Dialog<LinkedList<String>> dialog = new Dialog<>();
            tooltip.hide();

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
            //dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
            ((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);

            dialog.setTitle("Edge Editor");
            dialog.setHeaderText(null);
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(5);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField name = new TextField();
            name.setText(this.getRepresented().higherName());

            TextField length  = new TextField();
            length.setText(Integer.toString(this.getRepresented().getLength()));

            // Größe der Lupe festlegen
            Shape lupeEdge = Shapeable.createShape(imageUrl);
            lupeEdge.setScaleX(0.013);
            lupeEdge.setScaleY(0.013);

            // Erstelle den Button zum Suchen der Edge und formatiere ihn passend zum Delete-Button
            Button displayEdgeButton = new Button();
            displayEdgeButton.setStyle("-fx-min-width: 25px; " +
                    "-fx-min-height: 25px; " +
                    "-fx-max-width: 25px; " +
                    "-fx-max-height: 25px;");
            displayEdgeButton.setGraphic(lupeEdge);

            displayEdgeButton.setOnAction(event -> {
                parent.reOpenTexteditor().highlightLinesWithKeyWord(parent.getAbsSource().getDeltas(), this.getRepresented().higherName());
            });

            TooltipUtil.install(displayEdgeButton, new Tooltip(bundle.getString("search_in_abs")));


            // Erstelle den Button zum Suchen des Node1 und formatiere ihn passend zum Delete-Button
            Shape lupeNode1 = Shapeable.createShape(imageUrl);
            lupeNode1.setScaleX(0.013);
            lupeNode1.setScaleY(0.013);
            Button displayNode1Button = new Button();
            displayNode1Button.setStyle("-fx-min-width: 25px; " +
                    "-fx-min-height: 25px; " +
                    "-fx-max-width: 25px; " +
                    "-fx-max-height: 25px;");
            displayNode1Button.setGraphic(lupeNode1);

            displayNode1Button.setOnAction(event -> {
                parent.reOpenTexteditor().highlightLinesWithKeyWord(parent.getAbsSource().getDeltas(), this.getRepresented().getNode1().higherName());
            });
            TooltipUtil.install(displayNode1Button, new Tooltip(bundle.getString("search_in_abs")));

            // Erstelle den Button zum Suchen des Node2 und formatiere ihn passend zum Delete-Button
            Shape lupeNode2 = Shapeable.createShape(imageUrl);
            lupeNode2.setScaleX(0.013);
            lupeNode2.setScaleY(0.013);
            Button displayNode2Button = new Button();
            displayNode2Button.setStyle("-fx-min-width: 25px; " +
                    "-fx-min-height: 25px; " +
                    "-fx-max-width: 25px; " +
                    "-fx-max-height: 25px;");
            displayNode2Button.setGraphic(lupeNode2);

            displayNode2Button.setOnAction(event -> {
                parent.reOpenTexteditor().highlightLinesWithKeyWord(parent.getAbsSource().getDeltas(), this.getRepresented().getNode2().higherName());
            });
            TooltipUtil.install(displayNode2Button, new Tooltip(bundle.getString("search_in_abs")));

            grid.add(new Label("Name"), 0, 0);
            grid.add(name, 1, 0);
            grid.add(new Label("Length:"), 0, 1);
            grid.add(length, 1, 1);

            // Füge die DisplayButtons  nur hinzu, wenn es eine ABSSource war, da sonst der Texteditor nicht geöffnet werden kann
            // Beachte auch, dass er nur im Editor gesucht werden kann, wenn er schon in der ursprungsversion exisiterte
            if(parent.getAbsSource() != null){
                if(this.getRepresented().getOldName() != null) {grid.add(displayEdgeButton, 2, 0);}
                if(this.getRepresented().getNode1().getOldName() != null) {grid.add(displayNode1Button, 5,0);}
                if(this.getRepresented().getNode2().getOldName() != null) {grid.add(displayNode2Button, 5,1);}
            }



            grid.add(new Label ("Node 1:"), 3, 0);
            grid.add(new Label ("Node 2:"), 3, 1);
            grid.add(new Label (this.getRepresented().getNode1().higherName()), 4,0);
            grid.add(new Label (this.getRepresented().getNode2().higherName()), 4,1);


            dialog.getDialogPane().setContent(grid);
            dialog.setResultConverter(dialogButton -> {                                //get User input
                if (dialogButton == ButtonType.APPLY) {
                    LinkedList<String> result = new LinkedList<>();
                    //return new Pair<>(name.getText(), coordinates.getText());
                    result.add(name.getText());
                    result.add(length.getText());
                    return result;
                }
                return null;
            });

            Optional<LinkedList<String>> result = dialog.showAndWait();
            if(result.isPresent()){
                if (this.getRepresented().getName() != result.get().get(0) | this.getRepresented().setName(result.get().get(0))){

                }
                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setGraphic(null);
                    alert.setHeaderText(null);
                    alert.setContentText("Name already taken");
                    alert.showAndWait();
                    t.consume();
                    return;
                }
                Pattern d = Pattern.compile("\\d+");
                Matcher dm = d.matcher(result.get().get(1));
                if (dm.matches())
                {
                    int x =Integer.parseInt(dm.group());
                    this.getRepresented().setLength(x);
                }

                else{
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setGraphic(null);
                    alert.setHeaderText(null);
                    alert.setContentText("Invalid length");
                    alert.showAndWait();
                    t.consume();
                    return;
                }
                initializedShape(this.getShape());
                t.consume();

            }
        }
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
        String abs = "";
        if(getRepresented().getAbsName() != null)
            {abs = " | " + getRepresented().getAbsName();}
        tooltip=new Tooltip(getRepresented().getName() + " " + getRepresented().getLength() + "m" + abs);
        TooltipUtil.install(line,tooltip);

        if (this.getRepresented().getLength()>-1)line.setStroke(Color.BLACK);
    }

    @Nonnull
    @Override
    public Line createShape() {
        ChangeListener Node1Listener = ((observable, oldValue, newValue) ->
            relocate(this.getShape()));
        listeners.add(Node1Listener);
        ChangeListener Node2Listener = ((observable, oldValue, newValue) ->
            relocate(this.getShape()));
        listeners.add(Node2Listener);
        getRepresented().getNode1().movedProperty().addListener(new WeakChangeListener(Node1Listener));
        getRepresented().getNode2().movedProperty().addListener(new WeakChangeListener(Node2Listener));

        return new Line();
    }

    @Override
    protected Node createHighlight(Line node) {
        return createRectangleHighlight(node);
    }
}
