package com.github.bachelorpraktikum.visualisierbar.view.detail;

import com.github.bachelorpraktikum.visualisierbar.datasource.RestSource;
import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.view.DataSourceHolder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import javax.annotation.Nonnull;


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
    private Button breakButton;

    ElementDetails(Element element, ObservableIntegerValue time) {
        // this is executed first
        super(element, time, FXML_LOCATION);
        // this is executed AFTER initialize()

    }

    @FXML
    private void initialize() {
        //int i=5;
        /*
        if(this.getObject().getLogicalGroup()!=null) {
            for (Element element : this.getObject().getLogicalGroup().getElements()) {
            elementDetails.add(new Label(element.getName()) ,0,i);
            i++;
            }
        }

        if(this.getObject().getLogicalGroup()==null)
            GroupName.setText("Not in a Group");
        else
            GroupName.setText(this.getObject().getLogicalGroup().getName());
        */
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
