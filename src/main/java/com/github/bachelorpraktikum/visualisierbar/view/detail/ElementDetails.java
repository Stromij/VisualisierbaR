package com.github.bachelorpraktikum.visualisierbar.view.detail;

import com.github.bachelorpraktikum.visualisierbar.datasource.RestSource;
import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.GraphObject;
import com.github.bachelorpraktikum.visualisierbar.view.DataSourceHolder;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import javax.annotation.Nonnull;
import java.util.ArrayList;


class ElementDetails extends DetailsBase<Element> {

    private static final String FXML_LOCATION = "ElementDetails.fxml";

    @FXML
    private GridPane elementDetails;
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
        return elementDetails;
    }
}
