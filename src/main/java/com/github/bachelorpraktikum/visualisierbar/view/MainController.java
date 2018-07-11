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
import com.github.bachelorpraktikum.visualisierbar.view.graph.Junction;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.ProportionalCoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.SimpleCoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.legend.LegendListViewCell;
import com.github.bachelorpraktikum.visualisierbar.view.sourcechooser.AbsLines;
import com.github.bachelorpraktikum.visualisierbar.view.sourcechooser.SourceController;
import com.github.bachelorpraktikum.visualisierbar.view.texteditor.TexteditorController;
import com.github.bachelorpraktikum.visualisierbar.view.train.TrainView;
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
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.*;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MainController {

    private static final double SCALE_DELTA = 1.1;
    static private HashSet<Node> nodeClipboard = new HashSet<>();
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
    private Button reopenTextEditorButton;
    @FXML
    private Button playButton;
    @FXML
    private Button printToABSButton;
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
    private ChoiceBox deltas;
    @FXML
    private Slider NodeSizeSlider;
    @FXML
    private ToolBar standartTB;
    @FXML
    private Pane topPane;
    //EDITOR BUTTONS
    @FXML
    private ToolBar editorToolbar;
    @FXML
    private ChoiceBox<String> toolSelector;
    @FXML
    private Button deleteButton;
    @FXML
    private Button disconnectButton;
    @FXML
    private ToggleButton newNodeButton;
    @FXML
    private Button fcButton;
    private Rectangle selectionRec;

    @Nullable
    private Graph graph;
    private Map<Train, TrainView> trains;
    @Nullable
    private Highlightable lastHighlighted = null;
    private Stage stage;
    private double mousePressedX = -1;
    private double mousePressedY = -1;

    private double MousePositionX = 0;
    private double MousePositionY = 0;

    private boolean autoChange = false;
    private Pattern timePattern;

    @Nullable
    private AbsSource absSource;
    private File newAbsFile;


    private IntegerProperty simulationTime;
    private LinkedList<ChangeListener> listeners;

    private Stage editorStage;

    private TexteditorController textController;

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
        listeners = new LinkedList<>();


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
        reopenTextEditorButton.setOnAction(event -> {
            startTexteditor();
            reopenTextEditorButton.setManaged(false);
        });
        reopenTextEditorButton.setManaged(false);

        printToABSButton.setOnAction(event -> {
            if(graph != null && absSource != null)
                {absSource.refactorSource(graph, newAbsFile);
                 textController.reloadAll();
                }
            else if (graph != null) {
                 graph.printToAbs();
                }
        });

        SVGPath svgPlay = new SVGPath();
        svgPlay.setContent("M0 0 L12 7 L0 14 L0 0 Z");
        playButton.setGraphic(svgPlay);

        resetButton.setOnAction(event -> {
            simulationTime.set(Context.INIT_STATE_TIME);
            selectClosestLogEntry(Context.INIT_STATE_TIME);
        });
        infoButton.setOnAction(event -> showLicenceInfo());

        resetViewButton.setOnAction(event -> resetGraphView());

        proportionalToggle.setOnAction(ActionEvent -> switchGraph());

        deltas.setVisible(false);
        deltas.setManaged(false);
        //add Items that need to be in both Toolbars
        editorToolbar.getItems().add(resetViewButton);
        editorToolbar.getItems().add(legendButton);
        editorToolbar.getItems().add(printToABSButton);
        editorToolbar.getItems().add(deltas);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        editorToolbar.getItems().add(spacer);
        editorToolbar.getItems().add(closeButton);


        editorToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (graph == null) return;
            if (newValue) {

                if (!simulationStopWarning()) {
                    editorToggle.setSelected(false);
                    return;
                }
                topPane.getChildren().get(0).setVisible(false);
                topPane.getChildren().get(0).setManaged(false);
                topPane.getChildren().get(1).setVisible(true);
                topPane.getChildren().get(1).setManaged(true);

                startTexteditor();

                if (eventTraversal.isSelected()) {
                    eventTraversal.fire();
                }

                if (playToggle.isSelected()) {
                    playToggle.fire();
                    resetButton.fire();
                }
                if (proportionalToggle.isSelected()) {
                    proportionalToggle.fire();
                }
                resetButton.fire();
                graph.getNodes().forEach(((a, b) -> ((Junction) b).setMoveable(true)));
                rootPane.setLeft(null);

                Line x = new Line();                                                                                    //add marker for (0,0)
                Node zero = Node.in(graph.getContext()).create("zero", new Coordinates(0,0));
                Point2D cord= graph.getCoordinatesAdapter().apply(zero);
                graph.enterNode(zero);

                x.setStartX(cord.getX() -0.5);
                x.setEndX( cord.getX() + 0.5);
                x.setStartY(cord.getY());
                x.setEndY(cord.getY());
                x.setStrokeWidth(0.01);

                Line y = new Line();
                y.setStartY(cord.getY()-0.5);
                y.setEndY(cord.getY()+0.5);
                y.setStartX(cord.getX());
                y.setEndX(cord.getX());
                y.setStrokeWidth(0.01);
                graph.removeNode(zero);


                graph.getGroup().getChildren().addAll(x,y);


            } else {
                graph.getNodes().forEach(((a, b) -> ((Junction) b).setMoveable(false)));
                toolSelector.setValue(toolSelector.getItems().get(0));
            }
        });

        deleteButton.setOnAction((t) -> {
            Junction.getSelection().forEach((a) -> {
                if (graph == null) return;
                if (nodeClipboard.contains(a.getRepresentedObjects().get(0)))
                    nodeClipboard.remove(a.getRepresentedObjects().get(0));
                graph.removeNode(a.getRepresentedObjects().get(0));

            });
            Junction.emptySelection();
        });

        disconnectButton.setOnAction((t) -> Junction.getSelection().forEach((a) -> {
            if (graph == null) return;
            graph.disconnect(a.getRepresentedObjects().get(0));
        }));

        newNodeButton.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                centerPane.setCursor(Cursor.CROSSHAIR);

            } else {
                centerPane.setCursor(Cursor.DEFAULT);
            }
        });

        fcButton.setOnAction((t) -> {
            if (graph == null) return;
            HashSet<Node> nodeSet= new HashSet<>(Junction.getSelection().size());
            for(Junction j :Junction.getSelection()){
                nodeSet.add(j.getRepresentedObjects().get(0));
            }
            graph.fullyConnect(nodeSet);
        });
        // add Shortcut Handler
        rootPane.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            //editor shortcuts
            if (editorToggle.isSelected()) {
                if (event.getCode() == KeyCode.P && event.isControlDown()) {
                    printToABSButton.fire();
                }
                if (event.getCode() == KeyCode.D && event.isControlDown()) {
                    fcButton.fire();
                }
                if(event.getCode() == KeyCode.R && event.isControlDown())
                    {playButton.fire();}
                if (event.getCode() == KeyCode.N && event.isControlDown()) {
                    newNodeButton.fire();
                }
                if (event.getCode() == KeyCode.DELETE ||
                    event.getCode() == KeyCode.BACK_SPACE ) {
                    deleteButton.fire();
                }
                if (event.getCode() == KeyCode.C && event.isAltDown()) {
                    disconnectButton.fire();
                }
                if (event.getCode().isDigitKey()) {
                    if (event.getCode() == KeyCode.DIGIT1) {
                        toolSelector.setValue(toolSelector.getItems().get(0));
                    }
                    if (event.getCode() == KeyCode.DIGIT2) {
                        toolSelector.setValue(toolSelector.getItems().get(1));
                    }
                }
                if (event.getCode() == KeyCode.A && event.isControlDown()) {
                    if (graph != null) {
                        graph.getNodes().forEach((a, b) -> ((Junction) b).addToSelection());
                    }
                }
                if (event.getCode() == KeyCode.C && event.isControlDown()) {
                    nodeClipboard.clear();
                    HashSet<Node> selectedNodes = new HashSet<>();
                    for (Junction junction : Junction.getSelection()) {
                        selectedNodes.add(junction.getRepresentedObjects().get(0));
                    }
                    LinkedList<Node> nodeList = new LinkedList<>();
                    for (Junction a : Junction.getSelection()) {
                        Node copy = a.getRepresentedObjects().get(0);
                        nodeList.add(copy);
                    }
                    nodeClipboard.addAll(nodeList);

                }
                if (event.getCode() == KeyCode.V && event.isControlDown()) {
                    Node mostLeftNode = null;
                    for (Node node : nodeClipboard) {
                        if (mostLeftNode == null) {
                            mostLeftNode = node;
                        } else {
                            if (node.getCoordinates().getX() < mostLeftNode.getCoordinates().getX())
                                mostLeftNode = node;
                        }
                    }
                    if (mostLeftNode != null) {
                        Coordinates cord = Objects.requireNonNull(graph).getCoordinatesAdapter().reverse(new Point2D(MousePositionX, MousePositionY));
                        Coordinates mostLeftCord = mostLeftNode.getCoordinates();


                        RandomString gen = new RandomString(8, ThreadLocalRandom.current());
                        String name = null;
                        HashMap<Node, Node> copyNodes = new HashMap<>(Junction.getSelection().size());
                        HashMap<Element, LogicalGroup> groupCollector = new HashMap<>();
                        HashMap<Element, Element> alreadyCopiedElements = new HashMap<>();
                        HashMap<Element, LogicalGroup> gPCollector = new HashMap<>();
                        for (Node node : nodeClipboard) {

                            int x = node.getCoordinates().getX() - mostLeftCord.getX();
                            int y = node.getCoordinates().getY() - mostLeftCord.getY();

                            String newName = node.higherName().concat("_0");
                            for(int i = 0; Node.in(graph.getContext()).absNameExists(newName, null) || Node.in(graph.getContext()).NameExists(newName) ; i++)
                                {newName = node.higherName().concat("_").concat(String.valueOf(i));}

                            Node copy = Node.in(graph.getContext()).create(newName, new Coordinates(x + cord.getX(), y + cord.getY()));
                            copy.setAbsName(newName);
                            copyNodes.put(node, copy);
                            ((Junction) graph.getNodes().get(node)).setMoveable(true);

                        }
                        for (Node node : copyNodes.keySet()) {
                            for (Edge edge : node.getEdges()) {
                                if (copyNodes.containsKey(edge.getNode1()) && copyNodes.containsKey(edge.getNode2())) {
                                    String newName = edge.higherName().concat("_0");
                                    for(int i = 1; Edge.in(graph.getContext()).AbsNameExists(newName, null) || Edge.in(graph.getContext()).NameExists(newName); i++)
                                        {newName = edge.higherName().concat("_").concat(String.valueOf(i));}

                                    Edge copyEdge = Edge.in(graph.getContext()).create(newName, edge.getLength(), copyNodes.get(edge.getNode1()), copyNodes.get(edge.getNode2()));
                                    copyEdge.setAbsName(newName);
                                    copyNodes.get(node).addEdge(copyEdge);
                                }
                            }
                        }
                        for (Node node : copyNodes.keySet()) {
                            for (Element element : node.getElements()) {
                                if(alreadyCopiedElements.containsKey(element)) {continue;}
                                if (element.getType() == Element.Type.WeichenPunkt) {
                                    boolean test = true;
                                    for (Element switchEle : element.getSwitch().getElements()) {
                                        if (!copyNodes.containsKey(switchEle.getNode()))
                                            test = false;
                                    }
                                    if (!test) {
                                        continue;
                                    }
                                }
                                String newName = element.higherName().concat("_0");
                                for(int i = 0; Element.in(graph.getContext()).NameExists(newName) || Element.in(graph.getContext()).absNameExists(newName, null); i++)
                                    {newName = element.higherName().concat("_").concat(String.valueOf(i)); }

                                Element newElem = Element.in(graph.getContext()).create(newName, element.getType(), copyNodes.get(node), element.getState());
                                newElem.setAbsName(newName);
                                if(gPCollector.containsKey(element))
                                    {gPCollector.get(element).setBelongsTo(newElem);}
                                if (element.getLogicalGroup() != null) {
                                    // Prüfen ob es eine vollständige logische Gruppe ist oder nicht
                                    LogicalGroup grp = element.getLogicalGroup();
                                    String newGrpName = grp.getName().concat("_0");
                                    boolean completeLG = true;
                                    for(Element e: grp.getElements())
                                        {if(!nodeClipboard.contains(e.getNode()))
                                            {completeLG = false; break;}
                                        }
                                    if(!nodeClipboard.contains(grp.getBelongsTo().getNode()))
                                        {completeLG = false;}

                                    // Wenn es eine komplette logische Gruppe ist:
                                    if(completeLG) {
                                        if(groupCollector.containsKey(element))
                                            {newElem.setLogicalGroup(groupCollector.get(element));
                                             groupCollector.get(element).safeAddElement(newElem);
                                             groupCollector.remove(element);
                                            }
                                        else {
                                            // Suche nach einem geeigneten, verfügbaren Namen der neuen Gruppe
                                            for (int i = 1; LogicalGroup.in(graph.getContext()).NameExists(newGrpName); i++) {
                                                newGrpName = grp.getName().concat("_").concat(String.valueOf(i));
                                            }

                                            // Erstelle eine neue Gruppe unter dem gefundenen Namen
                                            LogicalGroup newGrp = LogicalGroup.in(graph.getContext()).create(newGrpName, grp.getKind());
                                            newGrp.safeAddElement(newElem);
                                            newGrp.setAdditional(grp.getAdditional());
                                            // Speichere für die kommenden Elemente die Zuweisung zu dieser LogicalGroup
                                            for(Element e: grp.getElements()){
                                                groupCollector.put(e, newGrp);
                                            }
                                            // Gefahrenpunkte müssen seperat gehandelt werden, da sie nicht direkter Bestandteil
                                            // einer Gruppe sind und nur über das belongTo-Attribut der Gruppe erreichbar sind

                                            if(grp.getBelongsTo().getType() == Element.Type.GefahrenPunkt)
                                                {// Wenn der Gefahrenpunkt schon kopiert wurde
                                                 if(alreadyCopiedElements.containsKey(grp.getBelongsTo()))
                                                    {newGrp.setBelongsTo(alreadyCopiedElements.get(grp.getBelongsTo()));}
                                                 // Wenn der Gefahrenpunkt noch nicht kopiert wurde:
                                                 else
                                                    {gPCollector.put(grp.getBelongsTo(), newGrp);}

                                                }

                                        }
                                    }
                                }
                                alreadyCopiedElements.put(element, newElem);
                                copyNodes.get(node).addElement(newElem);
                            }
                            graph.enterNode(copyNodes.get(node));
                            ((Junction) graph.getNodes().get(copyNodes.get(node))).setMoveable(true);
                        }


                    }
                }
            }
            if (event.getCode() == KeyCode.ESCAPE) {
                Junction.clearSelection();
            }
        });

        TooltipUtil.install(fcButton, new Tooltip(KeyCode.CONTROL.getName() + " + " + KeyCode.D.getName()));
        TooltipUtil.install(newNodeButton, new Tooltip(KeyCode.CONTROL.getName() + " + " + KeyCode.N.getName()));
        TooltipUtil.install(disconnectButton, new Tooltip(KeyCode.ALT.getName() + " + " + KeyCode.C.getName()));
        TooltipUtil.install(deleteButton, new Tooltip(KeyCode.DELETE.getName()));
        TooltipUtil.install(printToABSButton, new Tooltip(KeyCode.CONTROL.getName() + " + " + KeyCode.P.getName()));
        TooltipUtil.install(toolSelector, new Tooltip("use " + KeyCode.DIGIT1.getName() + " and " + KeyCode.DIGIT2.getName()+ " to switch between modes "));
        TooltipUtil.install(playButton, new Tooltip(KeyCode.CONTROL.getName() + " + " + KeyCode.R.getName()));
        NodeSizeSlider.valueProperty().bindBidirectional(Junction.getCALIBRATION_COEFFICIENT_prop());

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

        toolSelector.setValue(toolSelector.getItems().get(0));


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

        graphPane.setOnMouseMoved((event -> {
            Point2D c = Objects.requireNonNull(graph).getGroup().parentToLocal(new Point2D(event.getX(), event.getY()));
            MousePositionX = c.getX();
            MousePositionY = c.getY();
        }));

        graphPane.heightProperty().addListener(boundsListener);
        graphPane.widthProperty().addListener(boundsListener);

        graphPane.setOnMouseClicked((event) -> {
            Edge replace = null;
            if (newNodeButton.isSelected()) {
                if(event.getTarget() instanceof javafx.scene.shape.Line) {
                    Line edgeLine = (Line) event.getTarget();
                    for(Map.Entry<Edge, GraphShape<Edge>> entry : graph.getEdges().entrySet())
                        {Edge e = entry.getKey();
                         if(edgeLine.contains(e.getNode1().getCoordinates().getX(), e.getNode1().getCoordinates().getY()) &&
                            edgeLine.contains(e.getNode2().getCoordinates().getX(), e.getNode2().getCoordinates().getY()))
                            {replace = e; break;}
                        }
                }
                if (graph == null) return;
                //Search for available name
                String newName = "n0";
                for(int i=0; Node.in(graph.getContext()).absNameExists(newName, null) || Node.in(graph.getContext()).NameExists(newName); i++)
                    {newName = "n".concat(String.valueOf(i));}

                TextInputDialog dialog = new TextInputDialog(newName);
                dialog.setTitle("Enter Node Name");
                dialog.setGraphic(null);
                dialog.setHeaderText(null);
                dialog.showAndWait();
                String name = dialog.getResult();
                if (name == null) return;
                Point2D c = graph.getGroup().parentToLocal(new Point2D(event.getX(), event.getY()));
                try {
                    int newX = (int) Math.round(c.getX());
                    int newY = (int) Math.round(c.getY());
                    if (newX < 0 || newY < 0) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setX(event.getX());
                        alert.setY(event.getY());
                        alert.setGraphic(null);
                        alert.setHeaderText(null);
                        alert.setContentText("no negative coordinates allowed");
                        alert.showAndWait();
                        return;
                        /*
                        if (Math.round(c.getX()) < 0 && Math.round(c.getY()) >= 0) {
                            double x = 0 - Math.round(c.getX());
                            Map<Node, GraphShape<Node>> nodes = graph.getNodes();
                            for (Node n : nodes.keySet()) {
                                n.setCoordinates(new SimpleCoordinatesAdapter().reverse(new Point2D(( n.getCoordinates().getX() + (int) x), ( n.getCoordinates().getY()))));
                            }

                            graph.addNode(name, new Coordinates(0, (int) Math.round(c.getY())));
                        } else if (Math.round(c.getX()) >= 0 && Math.round(c.getY()) < 0) {
                            int y = 0 - (int) Math.round(c.getY());
                            Map<Node, GraphShape<Node>> nodes = graph.getNodes();
                            for (Node n : nodes.keySet()) {
                                n.setCoordinates(new Coordinates( n.getCoordinates().getX(),  n.getCoordinates().getY() +  y));
                            }

                            graph.addNode(name, new Coordinates((int) Math.round(c.getX()), 0));
                        } else {
                            int x = 0 - (int) Math.round(c.getX());
                            int y = 0 - (int) Math.round(c.getY());
                            Map<Node, GraphShape<Node>> nodes = graph.getNodes();
                            for (Node n : nodes.keySet()) {
                                n.setCoordinates(new Coordinates( n.getCoordinates().getX() + x,  n.getCoordinates().getY() + y));
                            }
                            graph.addNode(name, new Coordinates(0, 0));
                        }
                        */
                    } else {
                        graph.addNode(newName, new Coordinates(newX, newY));
                        Node.in(graph.getContext()).get(newName).setAbsName(newName);
                        if(replace != null)
                            {Node node1 = Node.in(graph.getContext()).get(replace.getNode1().getName());
                             Node node2 = Node.in(graph.getContext()).get(replace.getNode2().getName());
                             String firstName = replace.getName();
                             String firstNameTmp = firstName.concat("_0");
                             String firstAbsName = replace.higherName();
                             int length = replace.getLength();
                             String secondName = replace.higherName().concat("_0");
                             for(int i=0; Edge.in(graph.getContext()).AbsNameExists(secondName, null) || Edge.in(graph.getContext()).NameExists(secondName); i++)
                                {secondName = replace.higherName().concat("_").concat(String.valueOf(i));}
                             for(int i=0; Edge.in(graph.getContext()).AbsNameExists(firstNameTmp, null) || Edge.in(graph.getContext()).NameExists(firstNameTmp); i++)
                                {firstNameTmp = replace.getName().concat("_").concat(String.valueOf(i));}

                             // Prüfen, ob der neue Node auf einen alten "gerundet" wurde
                             Node isOnNode = null;
                             for(Node n : Node.in(graph.getContext()).getAll())
                                {if(n.getCoordinates().getX() == newX && n.getCoordinates().getY() == newY && !n.getAbsName().equals(newName))
                                    {isOnNode = n; break;}
                                }

                             // Wurde der neue Node auf einen alten "gerundet"?
                             if(isOnNode != null)
                                {// Errechne die direction, in die verschoben wird
                                 // true = vertical, false = horizontal
                                 boolean direction = Math.abs(event.getY() - newY) > Math.abs(event.getX() - newX);
                                 System.out.println("Y " + (newY - isOnNode.getCoordinates().getY() > 0 && direction));
                                 System.out.println("X " + (newX - isOnNode.getCoordinates().getX() >0 && !direction ));

                                 newY = newY - isOnNode.getCoordinates().getY() > 0 && direction ? newY +1 : newY;
                                 newX = newX - isOnNode.getCoordinates().getX() > 0 && !direction? newX +1 : newX;
                                 Node.in(graph.getContext()).get(newName).setCoordinates(new Coordinates(newX,newY));
                                 Node.in(graph.getContext()).get(newName).moved();


                                 for(Node n : Node.in(graph.getContext()).getAll())
                                    {if(direction)                      // Vertikale Verschiebung
                                        {if (n.getCoordinates().getX() == newX && n.getCoordinates().getY() >= newY && !n.getAbsName().equals(newName)) {
                                             Coordinates newCood = new Coordinates(n.getCoordinates().getX(), n.getCoordinates().getY() + 1);
                                             Node.in(graph.getContext()).get(n.getName()).setCoordinates(newCood);
                                             //n.moved();
                                            Node.in(graph.getContext()).get(n.getName()).moved();
                                            }
                                        }
                                     else {
                                        if (n.getCoordinates().getY() == newY && n.getCoordinates().getX() >= newX && !n.getAbsName().equals(newName)) {
                                            Coordinates newCood = new Coordinates(n.getCoordinates().getX() + 1, n.getCoordinates().getY());
                                            Node.in(graph.getContext()).get(n.getName()).setCoordinates(newCood);
                                            //n.moved();
                                            Node.in(graph.getContext()).get(n.getName()).moved();
                                        }

                                    }
                                    }
                                }

                             Node replaceNode1 = Node.in(graph.getContext()).get(replace.getNode1().getName());
                             Node replaceNode2 = Node.in(graph.getContext()).get(replace.getNode2().getName());


                             graph.addEdge(firstNameTmp, node1, Node.in(graph.getContext()).get(name), length, firstAbsName);
                             graph.addEdge(secondName, Node.in(graph.getContext()).get(name), node2, length, secondName);


                             // Hänge die direction für alle relevanten Elemente um

                             for(Element e : Node.in(graph.getContext()).get(replaceNode1.getName()).getElements())
                                {if(e.getDirection() == (replaceNode2))
                                    {Element.in(graph.getContext()).get(e.getName()).setDirection(Node.in(graph.getContext()).get(newName));}
                                }
                             for(Element e : Node.in(graph.getContext()).get(replaceNode2.getName()).getElements())
                                {if(e.getDirection() == (replaceNode1))
                                    {Element.in(graph.getContext()).get(e.getName()).setDirection(Node.in(graph.getContext()).get(newName));}
                                }

                             // Räume die Anzeige auf
                             graph.removeEdge(Edge.in(graph.getContext()).get(replace.getName()));
                             Edge.in(graph.getContext()).get(firstNameTmp).setName(firstName);

                             for(Node no : Node.in(graph.getContext()).getAll())
                                {Node.in(graph.getContext()).get(no.getName()).moved();}
                            }

                    }


                } catch (IllegalArgumentException e) {e.printStackTrace();
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
            }

        });


        graphPane.setOnMouseReleased(event -> {
            mousePressedX = -1;
            mousePressedY = -1;
            selectionRec.setVisible(false);
            if (toolSelector.getSelectionModel().getSelectedIndex() == 1) {       //select selected
                if (!event.isShiftDown()) Junction.clearSelection();
                if (graph == null) return;
                graph.getNodes().forEach((node, shape) -> {

                    Circle c = (Circle) shape.getShape();
                    Bounds sb = graph.getGroup().localToParent(c.getBoundsInParent());
                    double srx = selectionRec.getX();
                    double sry = selectionRec.getY();
                    if (srx < sb.getMaxX() && srx + selectionRec.getWidth() > sb.getMinX() && sry < sb.getMaxY() && sry + selectionRec.getHeight() > sb.getMinY()) {
                        if (event.isControlDown()) {
                            ((Junction) shape).removeFromSelection();
                        } else
                            ((Junction) shape).addToSelection();
                    }
                });
                selectionRec.setHeight(0);
                selectionRec.setWidth(0);
            }
        });

        selectionRec = new Rectangle();
        selectionRec.setVisible(false);
        selectionRec.setFill(Color.BLUE);
        selectionRec.setOpacity(0.2);
        graphPane.setOnMouseDragged(event -> {
            if (!event.isPrimaryButtonDown()) {
                return;
            }
            if (toolSelector.getSelectionModel().getSelectedIndex() == 0) {           //move selected
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
                }
            } else {
                if (mousePressedX == -1 && mousePressedY == -1) {
                    selectionRec.setVisible(true);
                    mousePressedX = event.getX();
                    mousePressedY = event.getY();
                    selectionRec.setX(mousePressedX);
                    selectionRec.setY(mousePressedY);
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

        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

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
            this.absSource = (AbsSource) source;
        }

        initializeDataSource(source);

        // Überprüfe, ob eine erneute Kompilierung ein anderes printToAbs liefern würde als die erste
        if (source instanceof AbsSource) {
            String firstComp = graph.printToAbs();

            newAbsFile = absSource.refactorSource(graph);
            String newCommand = String.format("absc -v -product=%s -erlang %s/*.abs -d %sgen/erlang/", absSource.getProduct(), newAbsFile, absSource.getParent().getPath());
            try
                {source = new AbsSource(newCommand, newAbsFile, absSource.getProduct());}
            catch (IOException e) {
                e.printStackTrace();
                ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                String headerText = bundle.getString("file_io_exception_header");
                alert.setHeaderText(headerText);
                String contentText = bundle.getString("second_file_not_openable");
                contentText = String.format(contentText, e.getMessage());
                alert.setContentText(contentText);
                alert.showAndWait();
            }

            initializeDataSource(source);

            String secondComp = graph.printToAbs();
            // falls sich die beiden printToAbs nicht gleichen weise den user darauf hin!
            if(!firstComp.equals(secondComp)){
                ResourceBundle bundle = ResourceBundle.getBundle("bundles.localization");
                String[] arrFirst = firstComp.split("\n", -1);
                String[] arrSecond = secondComp.split("\n", -1);

                // Wenn die Längen der beiden Arrays nicht übereinstimmen
                if(arrFirst.length != arrSecond.length)
                    {Alert alert = new Alert(Alert.AlertType.WARNING);
                     alert.setHeaderText(bundle.getString("file_io_exception_header"));
                     alert.setContentText(bundle.getString("error_length"));
                     alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
                     alert.initOwner(stage);
                     alert.showAndWait();
                    }
                //Sonst
                else
                    { // Definiere eine kleine Klasse für die Tabelle

                     ObservableList<AbsLines> data = FXCollections.observableArrayList();

                     // Suche alle unterschiedlichen Zeilen raus
                     for(int n = 0; n < arrFirst.length; n++)
                        {if(!arrFirst[n].equals(arrSecond[n]))
                            {data.add(new AbsLines(n, arrFirst[n], arrSecond[n]));}
                        }


                     Label label = new Label(bundle.getString("wrong_lines"));

                     // Generiere die Übersichtstabelle
                     TableView table = new TableView();
                     TableColumn lineNumber = new TableColumn("#");
                     lineNumber.setCellValueFactory(
                                new PropertyValueFactory<AbsLines,String>("line")
                        );
                     lineNumber.setMinWidth(40);
                     TableColumn firstCompTab = new TableColumn("First Output");
                     firstCompTab.setCellValueFactory(
                                new PropertyValueFactory<AbsLines,String>("first")
                        );
                     firstCompTab.setMinWidth(300);
                     TableColumn secondCompTab = new TableColumn("Second Output");
                     secondCompTab.setCellValueFactory(
                                new PropertyValueFactory<AbsLines,String>("second")
                        );
                     secondCompTab.setMinWidth(300);

                     // Fülle die Zellen mit den Daten
                     table.setItems(data);
                     table.getColumns().addAll(lineNumber, firstCompTab, secondCompTab);

                     table.setFixedCellSize(25);
                     table.setMaxHeight(350);
                     table.setPrefHeight(table.getItems().size()*25 + 28);


                     // Wrappe alles
                     VBox vbox = new VBox();
                     vbox.setSpacing(5);
                     vbox.setPadding(new Insets(20, 10, 0, 10));
                     vbox.getChildren().addAll(label, table);


                     // Gerneriere die Warnung an den User
                     Alert alert = new Alert(Alert.AlertType.WARNING);
                     alert.setHeaderText(bundle.getString("file_io_exception_header"));
                     alert.getDialogPane().setContent(vbox);
                     alert.showAndWait();


                    }
            }

        }
        showElements();
    }



    /**
     * Doing the initializing things for the graph
     * @param source the source to be initialized
     */
    private void initializeDataSource(DataSource source)
        {DataSourceHolder.getInstance().set(source);
         Context context = source.getContext();

         logList.setItems(context.getObservableEvents().sorted());
         fitGraphToCenter(getGraph());
         simulationTime.set(Context.INIT_STATE_TIME);
        }


    /**
     * Baut bei vorhandenen Deltas eine Choicebox in die GUI zum Auslesen der Deltas
     *
     * @param source AbsSource
     */

    private void initializeDeltas(@Nonnull AbsSource source) {
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
        //nodeClipboard.clear();
        if (graph == null) {
            Context context = DataSourceHolder.getInstance().getContext();
            if (proportionalToggle.isSelected()) {
                graph = new Graph(context, new ProportionalCoordinatesAdapter(context));
            } else {
                graph = new Graph(context, new SimpleCoordinatesAdapter());
            }
            graphPane.getChildren().add(graph.getGroup());
            showLegend();

            listeners.clear();
            ChangeListener<Boolean> GraphListener = ((observable, oldValue, newValue) -> {
                for (Map.Entry<Element, GraphShape<Element>> entry : graph.getElements().entrySet()) {
                    Element element = entry.getKey();
                    Shape elementShape = entry.getValue().getShape(element);

                    Binding<Boolean> binding = Bindings.createBooleanBinding(() ->
                                    element.getType().isVisible(elementShape.getBoundsInParent()),
                            element.getType().visibleStateProperty()
                    );
                    context.addObject(binding);
                    entry.getValue().getShape(element).visibleProperty().bind(binding);

                    elementShape.setOnMouseClicked(event ->
                        elementList.getSelectionModel().select(element));

                }

                showElements();
            });
            graph.changeProperty().addListener(GraphListener);
            listeners.add(new WeakChangeListener(GraphListener));

            graph.changed();


            for (Train train : Train.in(context).getAll()) {
                TrainView trainView = new TrainView(train, graph);
                trainView.timeProperty().bind(simulationTime);
                trains.put(train, trainView);
                trainView.setOnMouseClicked(e -> elementList.getSelectionModel().select(train));
            }
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

    private boolean simulationStopWarning() {
        if (Objects.requireNonNull(ConfigKey.simulationStoppedDoNotShowAgain.get()).equals("true")) {
            return true;
        }

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


        if (result.isPresent() && result.get() == buttonDoNotShowAgain) {
            ConfigKey.simulationStoppedDoNotShowAgain.set("true");
            return true;
        }
        return result.isPresent() && result.get() == buttonOK;
    }


    /**
     * Installs the Texteditor
     */
    private void startTexteditor(){
        if(absSource == null) return;
        FXMLLoader loader = new FXMLLoader(TexteditorController.class.getResource("TexteditorView.fxml"));
        ResourceBundle localizationBundle = ResourceBundle.getBundle("bundles.localization");
        editorStage = new Stage();
        loader.setResources(localizationBundle);



        try {loader.load();}
        catch(IOException e) {
            Logger.getLogger(getClass().getName()).severe("Editor window couldn't be opened\n" + e);
            return;
        }

        textController = loader.getController();
        textController.setSource(absSource);
        textController.setPath(newAbsFile);
        textController.setStage(editorStage, stage);

        editorStage.show();

        stage.requestFocus();

        editorStage.setOnCloseRequest(event -> {
            System.out.println("closed");
            reopenTextEditorButton.setManaged(true);
            editorStage = null;
        });
    }
}