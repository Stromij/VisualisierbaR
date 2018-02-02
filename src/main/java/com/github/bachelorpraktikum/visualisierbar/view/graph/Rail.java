package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.Edge;
import com.github.bachelorpraktikum.visualisierbar.view.TooltipUtil;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Rail extends SingleGraphShapeBase<Edge, Line> {

    private static final double CALIBRATION_COEFFICIENT = 0.05;
    private final List<ChangeListener> listeners;
    private Tooltip tooltip;

    protected Rail(Edge edge, CoordinatesAdapter adapter) {
        super(edge, adapter);
        listeners = new ArrayList<>(2);
        this.getShape().setOnMousePressed((t)->{
            if(t.isSecondaryButtonDown()){

                Dialog<LinkedList<String>> dialog = new Dialog<>();
                tooltip.hide();

                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
                dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL,ButtonType.APPLY);
                ((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setDefaultButton(true);

                dialog.setTitle("Edge Editor");
                dialog.setHeaderText(null);
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(5);
                grid.setPadding(new Insets(20, 150, 10, 10));
                TextField name = new TextField();
                name.setText(this.getRepresented().getName());
                TextField length  = new TextField();
                length.setText(Integer.toString(this.getRepresented().getLength()));
                grid.add(new Label("Name"), 0, 0);
                grid.add(name, 1, 0);
                grid.add(new Label("Length:"), 0, 1);
                grid.add(length, 1, 1);
                grid.add(new Label ("Node 1:"), 2, 0);
                grid.add(new Label ("Node 2:"), 2, 1);
                grid.add(new Label (this.getRepresented().getNode1().getName()), 3,0);
                grid.add(new Label (this.getRepresented().getNode2().getName()), 3,1);
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
        });
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
        tooltip=new Tooltip(getRepresented().getName() + " " + getRepresented().getLength() + "m");
        TooltipUtil.install(line,tooltip);

        if (this.getRepresented().getLength()>-1)line.setStroke(Color.BLACK);
    }

    @Nonnull
    @Override
    public Line createShape() {
        /*
        getRepresented().getNode1().movedProperty().addListener((observable, oldValue, newValue) -> {
            relocate(this.getShape());
        });

        getRepresented().getNode2().movedProperty().addListener((observable, oldValue, newValue) -> {
            relocate(this.getShape());
        });
        */

        ChangeListener Node1Listener = ((observable, oldValue, newValue) -> {
            relocate(this.getShape());
        });
        listeners.add(Node1Listener);
        ChangeListener Node2Listener = ((observable, oldValue, newValue) -> {
            relocate(this.getShape());
        });
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
