package com.github.bachelorpraktikum.visualisierbar.view;

import com.github.bachelorpraktikum.visualisierbar.CompositeObservableList;
import com.github.bachelorpraktikum.visualisierbar.FXCollectors;
import com.github.bachelorpraktikum.visualisierbar.Visualisierbar;
import com.github.bachelorpraktikum.visualisierbar.config.ConfigFile;
import com.github.bachelorpraktikum.visualisierbar.config.ConfigKey;
import com.github.bachelorpraktikum.visualisierbar.datasource.AbsSource;
import com.github.bachelorpraktikum.visualisierbar.datasource.DataSource;
import com.github.bachelorpraktikum.visualisierbar.datasource.RestSource;
import com.github.bachelorpraktikum.visualisierbar.model.*;
import com.github.bachelorpraktikum.visualisierbar.model.train.Train;
import com.github.bachelorpraktikum.visualisierbar.view.detail.DetailsBase;
import com.github.bachelorpraktikum.visualisierbar.view.detail.DetailsController;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import com.github.bachelorpraktikum.visualisierbar.view.graph.GraphShape;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.ProportionalCoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.SimpleCoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.legend.LegendListViewCell;
import com.github.bachelorpraktikum.visualisierbar.view.sourcechooser.SourceController;
import com.github.bachelorpraktikum.visualisierbar.view.train.TrainView;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Junction;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MainController {

    @FXML
    private AnchorPane detail;
    @FXML
    private ToggleButton eventTraversal;

    @FXML
    private ListView<GraphObject> elementList;
    @FXML
    private CheckBox elementFilter;
    @FXML
    private CheckBox trainFilter;
    @FXML
    private TextField filterText;
    @FXML
    private ToggleButton proportionalToggle;

    @FXML
    private ListView<Shapeable<?>> legend;
    @FXML
    private ToggleButton legendButton;
    @FXML
    private TextField velocityText;
    @FXML
    private ToggleButton playToggle;
    @FXML
    private Button closeButton;
    @FXML
    private BorderPane rootPane;

    @FXML
    private DetailsController detailBoxController;
    @FXML
    private Pane leftPane;
    @FXML
    private ToggleButton logToggle;
    @FXML
    private ToggleButton editorToggle;
    @FXML
    private ListView<Event> logList;
    @FXML
    private Button resetButton;
    @FXML
    private Button resetViewButton;
    @FXML
    private Button infoButton;
    @FXML
    private TextField timeText;
    @FXML
    private Button continueSimulation;
    @FXML
    private Label modelTime;
    @FXML
    private HBox rightSpacer;

    @FXML
    private Pane centerPane;
    @FXML
    private Pane graphPane;

    @FXML
    private ToolBar standartTB;
    @FXML
    private ChoiceBox<String> toolSelector;
    @FXML
    private ChoiceBox deltas;
    @FXML
    private Button deleteButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private ToggleButton newNodeButton;
    @FXML
    private Button fcButton;


    private Rectangle selectionRec;
    //private LinkedList<> selected;
    /*
    @FXML
    private ToolBar editorTB;
*/
    @Nullable
    private Graph graph;
    //private NumberAxis xAxis;
    //private NumberAxis yAxis;
    private Map<Train, TrainView> trains;
    @Nullable
    private Highlightable lastHighlighted = null;

    private Stage stage;

    private static final double SCALE_DELTA = 1.1;

    private double mousePressedX = -1;
    private double mousePressedY = -1;

    private boolean autoChange = false;
    private Pattern timePattern;

    private IntegerProperty simulationTime;
    /**
     * Is updated when simulationTime changes, but AFTER the model state has been updated
     */
    private ObservableIntegerValue postSimulationTime;
    private IntegerProperty velocity;
    private Animation simulation;
    private Timeline eventTraversalTimeline;

    @FXML
    private void initialize() {
        timePattern = Pattern.compile("(\\d+)(m?s?|h)?$");
        trains = new WeakHashMap<>();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);


        // START OF TIME RELATED INIT


        simulationTime = new SimpleIntegerProperty();
        IntegerProperty postSimulationTime = new SimpleIntegerProperty();
        this.postSimulationTime = postSimulationTime;
        simulationTime.addListener((observable, oldValue, newValue) -> {
            DataSourceHolder.getInstance().ifPresent(dataSource -> {
                Context context = dataSource.getContext();
                int oldInt = oldValue.intValue();
                int newInt = newValue.intValue();
                timeText.setText(String.format("%dms", newInt));
                Element.in(context).setTime(newInt);

                if (newInt > oldInt) {
                    boolean messages = Messages.in(context).fireEventsBetween(
                            node -> getGraph().getNodes().get(node).getShape(),
                            oldInt,
                            newInt
                    );
                    if (messages) {
                        playToggle.setSelected(false);
                    }
                }
            });
            postSimulationTime.setValue(newValue);
        });

        timeText.setOnAction(event -> {
            String text = timeText.getText();
            Matcher timeMatch = timePattern.matcher(text);
            int newTime = 0;

            if (timeMatch.find()) {
                try {
                    newTime = getMsFromString(text);
                } catch (NumberFormatException e) {
                    timeText.setText(simulationTime.get() + "ms");
                    return;
                }
            }

            simulationTime.set(newTime);
            selectClosestLogEntry(newTime);
        });
        timeText.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                timeText.setText(simulationTime.get() + "ms");
            }
        });

        this.simulation = new Timeline(new KeyFrame(Duration.millis(50), event -> {
            int time = (int) (simulationTime.get() + (velocity.get() * 0.05));
            simulationTime.set(time);
            selectClosestLogEntry(time);
        }));
        simulation.setCycleCount(Animation.INDEFINITE);

        eventTraversalTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            Event nextEvent = selectNextEvent(getCurrentTime());
            simulationTime.set(nextEvent.getTime());
        }));
        eventTraversal.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                eventTraversalTimeline.play();
            } else {
                eventTraversalTimeline.stop();
            }
            timeText.setDisable(newValue);
            playToggle.setDisable(newValue);
        });
        eventTraversalTimeline.setCycleCount(Timeline.INDEFINITE);

        // END OF TIME RELATED INIT

        fireOnEnterPress(closeButton);
        fireOnEnterPress(logToggle);
        fireOnEnterPress(editorToggle);
        closeButton.setOnAction(event -> showSourceChooser());
        resetButton.setOnAction(event -> {
            simulationTime.set(Context.INIT_STATE_TIME);
            selectClosestLogEntry(Context.INIT_STATE_TIME);
        });
        infoButton.setOnAction(event -> showLicenceInfo());

        resetViewButton.setOnAction(event -> resetGraphView());

        proportionalToggle.setOnAction(ActionEvent -> switchGraph());

        deltas.setVisible(false);
        deltas.setManaged(false);


        editorToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {


            if (graph.getNodes() == null) return;
            if (newValue) {

                if(!simulationStopWarning())
                    {editorToggle.setSelected(false);
                     return;
                    }



                /*
                proportionalToggle.setVisible(false);
                playToggle.setVisible(false);
                resetButton.setVisible(false);
                modelTime.setVisible(false);
                */
                standartTB.getItems().forEach((a) -> {
                    a.setVisible(false);
                });
                resetButton.setVisible(true);
                editorToggle.setVisible(true);
                closeButton.setVisible(true);
                //////////////////////////////////
                proportionalToggle.setVisible(true);
                toolSelector.setVisible(true);
                toolSelector.setManaged(true);
                deleteButton.setVisible(true);
                deleteButton.setManaged(true);
                disconnectButton.setManaged(true);
                disconnectButton.setVisible(true);
                newNodeButton.setManaged(true);
                newNodeButton.setVisible(true);
                fcButton.setManaged(true);
                fcButton.setVisible(true);



                /*if (proportionalToggle.isSelected()) {

                    proportionalToggle.fire();
                }*/
                if (eventTraversal.isSelected()) {
                    eventTraversal.fire();
                }

                if (playToggle.isSelected()) {
                    playToggle.fire();
                    resetButton.fire();

                }
                resetButton.fire();
                //coordinatesField.setVisible(true);
                graph.getNodes().forEach(((a, b) -> {
                    ((Junction) b).setMoveable(true);
                }));
                //logToggle.setVisible(false);
                rootPane.setLeft(null);
            } else {
                graph.getNodes().forEach(((a, b) -> {
                    ((Junction) b).setMoveable(false);
                }));

                graph.printToAbs();

                toolSelector.setManaged(false);
                deleteButton.setManaged(false);
                disconnectButton.setManaged(false);
                newNodeButton.setManaged(false);
                fcButton.setManaged(false);
                /*
                proportionalToggle.setVisible(true);
                playToggle.setVisible(true);
                logToggle.setVisible(true);
                //coordinatesField.setVisible(false);
                */
                standartTB.getItems().forEach((a) -> {
                    a.setVisible(true);
                });
                toolSelector.setVisible(false);
                deleteButton.setVisible(false);
                disconnectButton.setVisible(false);
                newNodeButton.setVisible(false);
                fcButton.setVisible(false);

                toolSelector.setValue(toolSelector.getItems().get(0));
            }


        });

        deleteButton.setOnAction((t) -> {
            Junction.getSelection().forEach((a) -> {

                graph.removeNode(a.getRepresentedObjects().get(0));

            });
            Junction.emptySelection();
        });

        disconnectButton.setOnAction((t) -> {
            Junction.getSelection().forEach((a) -> {
                graph.disconnect(a.getRepresentedObjects().get(0));
            });
        });

        newNodeButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                centerPane.setCursor(Cursor.CROSSHAIR);

            } else {
                centerPane.setCursor(Cursor.DEFAULT);
            }
        });

        fcButton.setOnAction((t) -> {
            graph.fullyConnect(Junction.getSelection());
        });
        /*
        newElementButton.setOnAction((t)->{
        });
        */


        legendButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            legend.setVisible(newValue);
            if (newValue) {
                legend.toFront();
            } else {
                legend.toBack();
            }
        });
        detailBoxController.setOnClose(event -> hideDetailView());

        // Hide logList by default
        rootPane.setLeft(null);
        logToggle.selectedProperty().addListener((observable, oldValue, newValue) ->
                rootPane.setLeft(newValue ? leftPane : null)
        );

        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        toolSelector.setItems(FXCollections.observableArrayList("move", "select"));
        toolSelector.setVisible(false);
        toolSelector.setValue(toolSelector.getItems().get(0));
        toolSelector.setManaged(false);


        initializeElementList();
        initializeLogList();
        initializeCenterPane();
        graphPane.getChildren().add(selectionRec);

        velocity = new SimpleIntegerProperty(1000);
        velocityText.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int newVelocity = Integer.parseUnsignedInt(newValue);
                velocity.set(newVelocity);
            } catch (NumberFormatException e) {
                velocityText.setText(oldValue);
            }
        });

        playToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                simulation.playFromStart();
            } else {
                simulation.stop();
            }
            timeText.setDisable(newValue);
            eventTraversal.setDisable(newValue);
        });

        DataSourceHolder.getInstance().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                boolean isRest = newValue instanceof RestSource;
                continueSimulation.setVisible(isRest);
                modelTime.setVisible(isRest);
            }
        });
        continueSimulation.setOnAction(event -> {
            RestSource source = (RestSource) DataSourceHolder.getInstance().get();
            continueSimulation.setDisable(true);
            source.continueSimulation();
            String text = ResourceBundle.getBundle("bundles.localization")
                    .getString("model_time");
            modelTime.setText(String.format(text, source.getTime()));
            continueSimulation.setDisable(false);
        });


    }

    private void initializeCenterPane() {
        ChangeListener<Number> boundsListener = (observable, oldValue, newValue) -> {
            if (DataSourceHolder.getInstance().isPresent()) {
                fitGraphToCenter(getGraph());
            }
        };
        graphPane.heightProperty().addListener(boundsListener);
        graphPane.widthProperty().addListener(boundsListener);

        graphPane.setOnMouseClicked((event) -> {
            if (newNodeButton.isSelected()) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Enter Node Name");
                dialog.setX(event.getX());
                dialog.setY(event.getY());
                dialog.setGraphic(null);
                dialog.setHeaderText(null);
                dialog.showAndWait();
                String name = dialog.getResult();
                if (name == null) return;
                //String name = dialog.getContentText();
                //System.out.println(name);
                //System.out.println(event.getX()+" "+ event.getY());
                //System.out.println(event.getSceneX()+" "+ event.getSceneY());
                Point2D c = graph.getGroup().parentToLocal(new Point2D(event.getX(), event.getY()));
                //System.out.println(c.getX()+" "+ c.getY());
                try {
                    graph.addNode(name, new Coordinates((int) Math.round(c.getX()), (int) Math.round(c.getY())));

                } catch (IllegalArgumentException e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setX(event.getX());
                    alert.setY(event.getY());
                    alert.setGraphic(null);
                    alert.setHeaderText(null);
                    alert.setContentText("Node already exists");
                    alert.showAndWait();
                }
                newNodeButton.setSelected(false);
                event.consume();
                //graph.addNode(name, new Coordinates(0,0));
            }


        });
        graphPane.setOnScroll(event -> {
            if (graph != null) {
                Group group = graph.getGroup();
                Bounds bounds = group.localToScene(group.getBoundsInLocal());

                double oldScale = group.getScaleX();
                double scaleFactor =
                        oldScale * ((event.getDeltaY() > 0) ? SCALE_DELTA : 1 / SCALE_DELTA);
                double translateX =
                        event.getScreenX() - (bounds.getWidth() / 2 + bounds.getMinX());
                double translateY =
                        event.getScreenY() - (bounds.getHeight() / 2 + bounds.getMinY());
                double factor = (scaleFactor / oldScale) - 1;


                group.setScaleX(scaleFactor);
                group.setScaleY(scaleFactor);
                group.setTranslateX(group.getTranslateX() - factor * translateX);
                group.setTranslateY(group.getTranslateY() - factor * translateY);
                //yAxis.setScaleX(scaleFactor);
                //yAxis.setScaleY(scaleFactor);
                //yAxis.setTranslateX(yAxis.getTranslateX() - factor * translateX);
                //yAxis.setTranslateY(yAxis.getTranslateY() - factor/4 * translateY);

                //yAxis.setLayoutX(yAxis.getLayoutX() * 1+(scaleFactor-oldScale)/2 );
                //yAxis.setLayoutY(yAxis.getLayoutY() * 1+(scaleFactor-oldScale)/2 );
                //yAxis.setUpperBound(yAxis.getUpperBound()*scaleFactor);

            }

        });


        graphPane.setOnMouseReleased(event -> {
            mousePressedX = -1;
            mousePressedY = -1;
            selectionRec.setVisible(false);
            if (toolSelector.getValue().equals("select")) {
                //System.out.println( selectionRec.getBoundsInLocal()
                if (!event.isShiftDown()) Junction.clearSelection();
                if (graph.getNodes() == null) return;
                graph.getNodes().forEach((node, shape) -> {

                    Circle c = (Circle) shape.getShape();
                    Bounds sb = graph.getGroup().localToParent(c.getBoundsInParent());
                    //graph.getGroup().get
                    double srx = selectionRec.getX();
                    double sry = selectionRec.getY();
                    //if(shape.getShape()!=null && (selectionRec.getX()<shape.getShape()) && (selectionRec.getX()+selectionRec.getWidth()>shape.getShape().getLayoutX()) && (selectionRec.getY()<shape.getShape().getLayoutY())&&(selectionRec.getY()+selectionRec.getHeight()>shape.getShape().getLayoutX()))
                    if (srx < sb.getMaxX() && srx + selectionRec.getWidth() > sb.getMinX() && sry < sb.getMaxY() && sry + selectionRec.getHeight() > sb.getMinY()) {
                        //System.out.println("things are happening");
                        ((Junction) shape).addToSelection();
                        //selected.getChildren().add(shape.getShape());
                    }
                });
            }
            //if (selected.getChildren().size()>0)
        });

        selectionRec = new Rectangle();
        selectionRec.setVisible(false);
        selectionRec.setFill(Color.BLUE);
        selectionRec.setOpacity(0.2);
        //selected=new Group();
        graphPane.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) {
                return;
            }
            if (toolSelector.getValue().equals("move")) {
                if (mousePressedX == -1 && mousePressedY == -1) {
                    mousePressedX = event.getX();
                    mousePressedY = event.getY();
                }

                double xOffset = (event.getX() - mousePressedX);
                double yOffset = (event.getY() - mousePressedY);

                graphPane.setTranslateX(graphPane.getTranslateX() + xOffset);
                graphPane.setTranslateY(graphPane.getTranslateY() + yOffset);
                if (graph != null) {
                    graph.getGroup().getScaleY();
                    //yAxis.setTranslateY(yAxis.getTranslateY() - yOffset / graph.getGroup().getScaleY());
                    //yAxis.setTranslateX(yAxis.getTranslateX() - xOffset);
                }
            } else {
                if (mousePressedX == -1 && mousePressedY == -1) {
                    //System.out.println("CLICK");
                    selectionRec.setVisible(true);
                    mousePressedX = event.getX();
                    mousePressedY = event.getY();
                    selectionRec.setX(mousePressedX);
                    selectionRec.setY(mousePressedY);
                    //selected.getChildren().removeAll();
                }
                double xOffset = (event.getX() - mousePressedX);
                double yOffset = (event.getY() - mousePressedY);
                if (xOffset < 0) {
                    selectionRec.setX(mousePressedX + xOffset);
                } else {
                    selectionRec.setX(mousePressedX);
                }
                if (yOffset < 0) {
                    selectionRec.setY(mousePressedY + yOffset);
                } else {
                    selectionRec.setY(mousePressedY);
                }
                selectionRec.setWidth(Math.abs(xOffset));
                selectionRec.setHeight(Math.abs(yOffset));


            }
            event.consume();
        });
        //graphPane.getChildren().add(selectionRec);
        MenuItem exportItem = new MenuItem("Export");
        exportItem.setOnAction(event -> exportGraph());
        ContextMenuUtil.attach(centerPane, Collections.singletonList(exportItem));
    }

    private void initializeLogList() {
        StringConverter<Event> stringConverter = new StringConverter<Event>() {
            @Override
            public String toString(Event event) {
                return event.getDescription();
            }

            @Override
            public Event fromString(String string) {
                return null;
            }
        };

        Callback<ListView<Event>, ListCell<Event>> textFactory = TextFieldListCell
                .forListView(stringConverter);

        AtomicReference<Binding<Paint>> paintBinding = new AtomicReference<>();
        Callback<ListView<Event>, ListCell<Event>> listCellFactory = cell -> {
            ListCell<Event> result = textFactory.call(cell);
            Paint defaultTextFill = result.getTextFill();
            result.itemProperty().addListener(((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    Binding<Paint> warningBinding = Bindings.createObjectBinding(
                            () -> newValue.getWarnings().isEmpty() ?
                                    defaultTextFill : Color.rgb(255, 0, 0),
                            newValue.getWarnings()
                    );
                    paintBinding.set(warningBinding);
                    result.textFillProperty().bind(warningBinding);
                }
            }));

            TooltipUtil.install(result, () -> {
                StringBuilder sb = new StringBuilder();
                if (result.getItem() != null) {
                    sb.append(result.getItem().getDescription());
                    for (String warning : result.getItem().getWarnings()) {
                        sb.append(System.lineSeparator()).append("[WARN]: ").append(warning);
                    }
                }
                return sb.toString();
            });

            return result;
        };

        logList.setCellFactory(listCellFactory);
        logList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!autoChange) {
                        simulationTime.set(newValue.getTime());
                    }
                    Context context = DataSourceHolder.getInstance().getContext();
                    Element.in(context).setTime(newValue.getTime());
                }
        );
    }

    private void initializeElementList() {
        StringConverter<GraphObject> stringConverter = new StringConverter<GraphObject>() {
            @Override
            public String toString(GraphObject object) {
                return object.getName();
            }

            @Override
            public GraphObject fromString(String string) {
                return null;
            }
        };
        Callback<ListView<GraphObject>, ListCell<GraphObject>> textFactory = TextFieldListCell
                .forListView(stringConverter);
        Callback<ListView<GraphObject>, ListCell<GraphObject>> elementListCellFactory =
                (listView) -> {
                    ListCell<GraphObject> cell = textFactory.call(listView);
                    TooltipUtil.install(cell, () -> {
                        if (cell.getItem() != null) {
                            return cell.getItem().getName();
                        } else {
                            return "";
                        }
                    });
                    return cell;
                };

        elementList.setCellFactory(elementListCellFactory);
        elementList.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (lastHighlighted != null) {
                        lastHighlighted.highlightedProperty().set(false);
                    }

                    if (newValue == null) {
                        return;
                    }

                    if (newValue instanceof Element) {
                        Element elemnt = (Element) newValue;
                        lastHighlighted = getGraph().getElements().get(elemnt);
                    } else {
                        Train train = (Train) newValue;
                        lastHighlighted = trains.get(train);
                    }
                    lastHighlighted.highlightedProperty().set(true);

                    setDetail(DetailsBase.create(newValue, postSimulationTime, centerPane));
                });
    }


    private int getLastEventIndex(int time) {
        int last = 0;
        List<Event> items = logList.getItems();
        for (int index = 0; index < items.size(); index++) {
            Event item = items.get(index);
            if (item.getTime() > time) {
                return last;
            }
            last = index;
        }
        // Return last
        return last;
    }

    private Event selectNextEvent(int time) {
        int index = getLastEventIndex(time) + 1;
        if (index >= logList.getItems().size()) {
            index--;
        }

        Event event = logList.getItems().get(index);
        selectEvent(event);
        return event;
    }

    private Event selectClosestLogEntry(int time) {
        Event event = logList.getItems().get(getLastEventIndex(time));
        selectEvent(event);
        return event;
    }

    private void selectEvent(Event event) {
        autoChange = true;
        logList.getSelectionModel().select(event);
        logList.scrollTo(event);
        autoChange = false;
    }

    private int getMsFromString(String timeString) {
        int ms = -1;

        Matcher timeMatch = timePattern.matcher(timeString);
        String type = "ms";
        int time = ms;

        if (timeMatch.find()) {
            String typeMatch = timeMatch.group(2);
            if (typeMatch != null) {
                type = typeMatch;
            }

            time = Integer.valueOf(timeMatch.group(1));
        }

        switch (type) {
            case "s":
                ms = time * 1000;
                break;
            case "m":
                ms = time * 1000 * 60;
                break;
            case "h":
                ms = time * 1000 * 60 * 60;
                break;
            default:
                ms = time;
        }

        return ms;
    }

    private void showDetailView() {
        detail.toFront();
        legend.toBack();
        elementList.toBack();
    }

    private void hideDetailView() {
        detail.toBack();
        elementList.toFront();
        elementList.getSelectionModel().clearSelection();
    }

    /**
     * Adds an EventHandler to the button which fires the button on pressing enter.
     *
     * @param button Button to add eventHandler to
     */
    private void fireOnEnterPress(ButtonBase button) {
        button.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                button.fire();
            }
        });
    }

    public void setStage(@Nonnull Stage stage) {
        this.stage = stage;
        stage.setScene(new Scene(rootPane));

        stage.centerOnScreen();
        stage.setMaximized(false);
        stage.setMaximized(true);
    }

    private void showLegend() {
        Context context = DataSourceHolder.getInstance().getContext();
        ObservableList<Shapeable<?>> items = Stream.concat(
                Train.in(context).getAll().stream(),
                Element.in(context).getAll().stream()
                        .map(Element::getType)
                        .distinct()
        ).collect(FXCollectors.toObservableList());

        legend.setItems(items);
        legend.setCellFactory(studentListView -> new LegendListViewCell());
    }

    private int getCurrentTime() {
        return simulationTime.get();
    }

    private void showElements() {
        Context context = DataSourceHolder.getInstance().getContext();

        FilteredList<Train> trains = FXCollections.observableList(
                new ArrayList<>(Train.in(context).getAll())
        ).filtered(null);
        ObservableValue<Predicate<Train>> trainBinding = Bindings.createObjectBinding(
                () -> s -> trainFilter.isSelected(),
                trainFilter.selectedProperty()
        );
        context.addObject(trainBinding);
        trains.predicateProperty().bind(trainBinding);

        FilteredList<Element> elements = FXCollections.observableList(
                new ArrayList<>(Element.in(context).getAll())
        ).filtered(null);
        ObservableValue<Predicate<Element>> elementBinding = Bindings.createObjectBinding(
                () -> s -> elementFilter.isSelected(),
                elementFilter.selectedProperty());
        context.addObject(elementBinding);
        elements.predicateProperty().bind(elementBinding);

        ObservableList<GraphObject> items = new CompositeObservableList<>(trains, elements);
        FilteredList<GraphObject> textFilteredItems = items.filtered(null);
        ObservableValue<Predicate<GraphObject>> textFilterBinding = Bindings
                .createObjectBinding(() -> {
                    String text = filterText.getText().trim().toLowerCase();
                    return s -> s.getName().toLowerCase().contains(text);
                }, filterText.textProperty());
        context.addObject(textFilterBinding);
        textFilteredItems.predicateProperty().bind(textFilterBinding);

        elementList.setItems(textFilteredItems);
    }

    public void setDataSource(@Nonnull DataSource source) {

        if (source instanceof AbsSource) {
            initializeDeltas((AbsSource) source);
        }

        DataSourceHolder.getInstance().set(source);
        Context context = source.getContext();
        logList.setItems(context.getObservableEvents().sorted());
        fitGraphToCenter(getGraph());
        simulationTime.set(Context.INIT_STATE_TIME);
        showElements();
    }


    /**
     * Baut bei vorhandenen Deltas eine Choicebox in die GUI zum Auslesen der Deltas
     *
     * @param source AbsSource
     */

    private void initializeDeltas(AbsSource source) {
        ArrayList<String> deltaArray = source.getDeltas();
        if (deltaArray.size() <= 0) {
            return;
        }
        this.deltas.setItems(FXCollections.observableArrayList(deltaArray));
        this.deltas.setVisible(true);
        this.deltas.setManaged(true);
        this.deltas.setValue(deltaArray.get(0));
    }

    /**
     * Gets the current graph shape.<br>
     * If no graph shape exists, this method creates one and returns it.
     *
     * @return the graph shape
     * @throws IllegalStateException if there is no context
     */
    @Nonnull
    private Graph getGraph() {
        if (graph == null) {
            Context context = DataSourceHolder.getInstance().getContext();
            if (proportionalToggle.isSelected()) {
                //TODO: carry over edited Context? Context History for undo?
                graph = new Graph(context, new ProportionalCoordinatesAdapter(context));
            } else {
                graph = new Graph(context, new SimpleCoordinatesAdapter());
            }
            graphPane.getChildren().add(graph.getGroup());
            showLegend();

            for (Map.Entry<Element, GraphShape<Element>> entry : graph.getElements().entrySet()) {
                Element element = entry.getKey();
                Shape elementShape = entry.getValue().getShape(element);
                Binding<Boolean> binding = Bindings.createBooleanBinding(() ->
                                element.getType().isVisible(elementShape.getBoundsInParent()),
                        element.getType().visibleStateProperty()
                );
                context.addObject(binding);
                entry.getValue().getShape(element).visibleProperty().bind(binding);
                elementShape.setOnMouseClicked(event -> {
                    elementList.getSelectionModel().select(element);
                });
            }
            for (Train train : Train.in(context).getAll()) {
                TrainView trainView = new TrainView(train, graph);
                trainView.timeProperty().bind(simulationTime);
                trains.put(train, trainView);
                trainView.setOnMouseClicked(e -> elementList.getSelectionModel().select(train));
            }
            /*
            Bounds graphBounds = graph.getGroup().getBoundsInParent();
            yAxis=new NumberAxis(0,graphBounds.getHeight(),1);
            yAxis.setSide(Side.LEFT);
            yAxis.setPrefHeight(graphPane.getHeight());
            yAxis.setMinorTickVisible(false);
            yAxis.setLayoutY(graph.getGroup().getLayoutY());
            //graph.getGroup().getChildren().add(yAxis);
            graphPane.getChildren().add(yAxis);
            */
        }


        return graph;
    }

    private void setDetail(DetailsBase<? extends GraphObject<?>> detail) {
        showDetailView();
        detailBoxController.setDetail(detail);
    }

    private void fitGraphToCenter(Graph graph) {
        Bounds graphBounds = graph.getGroup().getBoundsInParent();
        double widthFactor = (graphPane.getWidth()) / graphBounds.getWidth();
        double heightFactor = (graphPane.getHeight()) / graphBounds.getHeight();

        double scaleFactor = Math.min(widthFactor, heightFactor);

        if (!Double.isFinite(scaleFactor)) {
            scaleFactor = 1;
        }

        if (scaleFactor <= 0) {
            scaleFactor = 1;
        }

        graph.scale(scaleFactor);
        moveGraphToCenter(graph);
    }

    private void moveGraphToCenter(Graph graph) {
        Bounds graphBounds = graph.getGroup().getBoundsInParent();

        double finalX = (graphPane.getWidth() - graphBounds.getWidth()) / 2;
        double xTranslate = finalX - graphBounds.getMinX();

        double finalY = (graphPane.getHeight() - graphBounds.getHeight()) / 2;
        double yTranslate = finalY - graphBounds.getMinY();

        graph.move(xTranslate, yTranslate);
    }

    private void showSourceChooser() {
        cleanUp();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(
                "sourcechooser/SourceChooser.fxml"));
        loader.setResources(ResourceBundle.getBundle("bundles.localization"));
        try {
            loader.load();
        } catch (IOException e) {
            // This should never happen, because the location is set (see load function)
            return;
        }
        SourceController controller = loader.getController();
        controller.setStage(stage);
    }

    private void resetGraphView() {
        fitGraphToCenter(getGraph());
        graphPane.setTranslateX(0);
        graphPane.setTranslateY(0);
        //yAxis.setTranslateX(0);
        //yAxis.setTranslateY(0);
    }


    private void cleanUp() {
        stage.setMaximized(false);
        if (graph != null) {
            simulation.stop();
            graphPane.getChildren().clear();
            graph = null;
        }
        DataSourceHolder.getInstance().set(null);
    }

    private void switchGraph() {
        GraphObject selected = elementList.getSelectionModel().getSelectedItem();
        graphPane.getChildren().clear();
        //TODO: List of persistend Objects?
        graphPane.getChildren().add(selectionRec);
        graph = null;
        fitGraphToCenter(getGraph());
        if (selected != null) {
            // reselect element
            elementList.getSelectionModel().select(null);
            elementList.getSelectionModel().select(selected);
        }
    }

    private void showLicenceInfo() {
        Visualisierbar.showLicenceInfo();
    }

    private void exportGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("gnuplot (*.dat)", "*.dat"),
                new FileChooser.ExtensionFilter("PNG Image (*.png)", "*.png"),
                new FileChooser.ExtensionFilter("JPEG Image (*.jpg)", "*.jpg")
        );
        String initDirString = ConfigFile.getInstance().getProperty(
                ConfigKey.initialLogFileDirectory.getKey(),
                System.getProperty("user.home")
        );
        File initDir = new File(initDirString);
        fileChooser.setInitialDirectory(initDir);
        fileChooser.setInitialFileName("Graph");

        File file = fileChooser.showSaveDialog(rootPane.getScene().getWindow());

        if (file != null) {
            Exporter.exportGraph(this.getGraph(), file);
        }
    }

    private boolean simulationStopWarning()
        {if(ConfigKey.simulationStoppedDoNotShowAgain.get().equals("true"))
            {return true;}

         ButtonType buttonDoNotShowAgain = new ButtonType("Do not show Again");
         ButtonType buttonOK = new ButtonType("Okay");
         ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

         Alert simulationInfo = new Alert(Alert.AlertType.WARNING);
         simulationInfo.setTitle("Simulation stopped");
         simulationInfo.setHeaderText("Simulation will be stopped!");
         simulationInfo.setContentText("Attention!\n" +
                                       "By opening the editor simulations will be stopped.\n" +
                                       "After opening the editor there will be no simulations possible!");
         simulationInfo.getDialogPane().setPrefWidth(525);
         simulationInfo.getDialogPane().setMaxWidth(Double.MAX_VALUE);
         simulationInfo.getButtonTypes().setAll(buttonDoNotShowAgain, buttonOK, buttonCancel);
         Optional<ButtonType> result = simulationInfo.showAndWait();

         System.out.println(result);
         if (result.get() == buttonDoNotShowAgain)
            {ConfigKey.simulationStoppedDoNotShowAgain.set("true");
             return true;
            }
         else if(result.get() == buttonOK)
            {return true;}
         else
            {return false;}
        }
}