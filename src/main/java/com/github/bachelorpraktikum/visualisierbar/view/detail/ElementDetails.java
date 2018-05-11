package com.github.bachelorpraktikum.visualisierbar.view.detail;

import com.github.bachelorpraktikum.visualisierbar.datasource.RestSource;
import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.GraphObject;
import com.github.bachelorpraktikum.visualisierbar.model.LogicalGroup;
import com.github.bachelorpraktikum.visualisierbar.view.DataSourceHolder;
import com.github.bachelorpraktikum.visualisierbar.view.Highlightable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedList;


class ElementDetails extends DetailsBase<Element> {

    private static final String FXML_LOCATION = "ElementDetails.fxml";

    @FXML
    private GridPane detailPane;
    @FXML
    private GridPane elementDetails;
    @FXML
    private GridPane groupElements;
    @FXML
    private Label coordinates;
    @FXML
    private Label typeValue;
    @FXML
    private Label stateValue;
    @FXML
    private Label GroupName;
    @FXML
    private Label Kind;
    @FXML
    private Label lGKind;
    @FXML
    private Button breakButton;


    ElementDetails(Element element, ObservableIntegerValue time) {
        // this is executed first
        super(element, time, FXML_LOCATION);
        // this is executed AFTER initialize()

    }

    @FXML
    private void initialize() {


        ListView<GraphObject> ElementList = new ListView<>();
        if(getObject().getLogicalGroup()!=null){
            ObservableList objects = FXCollections.observableList(getObject().getLogicalGroup().getElements());

            ElementList.setCellFactory(new Callback<ListView<GraphObject>,ListCell<GraphObject>> (){
                @Override
                public ListCell<GraphObject> call(ListView<GraphObject> arg0) {
                    ListCellX<GraphObject> cell = new ListCellX<GraphObject>(){
                        @Override
                        public void updateItem(GraphObject item,boolean empty){
                            super.updateItem(item,empty);
                            if(item!=null){
                            setGraphic(new Label(item.higherName()));
                            }
                        }
                    };

                    cell.init(objects);

                    cell.setOnMouseEntered((a)->{
                        GraphObject element= cell.getItem();
                        if(element!=null)
                            cell.getItem().getGraph().getElements().get(cell.getItem()).highlightedProperty().set(true);
                    });
                    cell.setOnMouseExited((a)->{
                        GraphObject element= cell.getItem();
                        if(element!=null)
                            cell.getItem().getGraph().getElements().get(cell.getItem()).highlightedProperty().set(false);
                    });



                    return cell;
                }
            });
            groupElements.add(ElementList,0,0);
            ElementList.setItems(objects);
            ToggleButton markAllToggle = new ToggleButton();
            markAllToggle.setText("Mark all Elements");
            markAllToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue){
                    for(Element element: getObject().getLogicalGroup().getElements()){
                        element.getGraph().getElements().get(element).highlightedProperty().set(true);
                    }
                }
                else{
                    for(Element element: getObject().getLogicalGroup().getElements()){
                        element.getGraph().getElements().get(element).highlightedProperty().set(false);

                    }
                }
            });
            groupElements.add(markAllToggle,0,1);
            Label platzhalter = new Label();
            groupElements.add(platzhalter,0,2);

            Label AdditionalLb = new Label();
            if(this.getObject().getLogicalGroup().getKind() == LogicalGroup.Kind.LIMITER)
                {AdditionalLb.setText("Limit:");}
            if(this.getObject().getLogicalGroup().getKind() == LogicalGroup.Kind.SIGNAL)
                {AdditionalLb.setText("ActiveZugFolge:");}
            if(this.getObject().getLogicalGroup().getKind() == LogicalGroup.Kind.SWITCH)
                {AdditionalLb.setText("Direction:");}

            TextField additionalTF = new TextField();
            additionalTF.setText(this.getObject().getLogicalGroup().getAdditional());

            additionalTF.textProperty().addListener((observable, oldValue, newValue) -> {
                this.getObject().getLogicalGroup().setAdditional(newValue);
            });
            groupElements.add(AdditionalLb, 0,3);
            groupElements.add(additionalTF,0,4);


        }



        if(this.getObject().getLogicalGroup()==null){
            GroupName.setText("Not in a Group");
            Kind.setVisible(false);
        }
        else{
            GroupName.setText(this.getObject().getLogicalGroup().getName());
            lGKind.setText(this.getObject().getLogicalGroup().getKind().toString());
        }

        coordinates.setText(
            getCoordinatesString(getObject().getNode().getCoordinates().toPoint2D())
        );
        typeValue.setText(getObject().getType().getName());

        ChangeListener<Number> stateListener =
            (observable, oldValue, newValue) -> stateValue.setText(getObject().getState().name());
        addBinding(stateListener);
        timeProperty().addListener(new WeakChangeListener<>(stateListener));
        stateListener.changed(null, null, null);

        DataSourceHolder dataSourceHolder = DataSourceHolder.getInstance();
        BooleanBinding isRestSource = Bindings.createBooleanBinding(
            () -> dataSourceHolder.isPresent() && dataSourceHolder.get() instanceof RestSource,
            dataSourceHolder
        );
        addBinding(isRestSource);

        BooleanBinding isRestSourceAndCanBeBroken = Bindings.createBooleanBinding(
            () -> {
                Element element = getObject();
                if (isRestSource.get()) {
                    RestSource restSource = (RestSource) dataSourceHolder.get();
                    return restSource.hasSignal(element);
                } else {
                    return false;
                }
            },
            isRestSource
        );
        addBinding(isRestSourceAndCanBeBroken);

        breakButton.visibleProperty().bind(isRestSourceAndCanBeBroken);
        breakButton.setOnAction(event -> {
            breakButton.setDisable(true);
            RestSource dataSource = (RestSource) DataSourceHolder.getInstance().get();
            // maybe show some success message? or make method accept onFailure to re-enable button?
            dataSource.breakElement(getObject(), null);
        });

        if(isRestSourceAndCanBeBroken.get()) {
            RestSource restSource = (RestSource) DataSourceHolder.getInstance().get();
            restSource.fetchIsBroken(getObject(), isBroken -> breakButton.setDisable(isBroken));
        }
    }

    @Nonnull
    @Override
    Node getDetails() {
        return detailPane;
    }
}
