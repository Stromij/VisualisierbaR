package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.datasource.RestSource;
import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.view.DataSourceHolder;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.WeakChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javax.annotation.Nonnull;


public class ElementDetails extends DetailsBase<Element> {

    private static final String FXML_LOCATION = "ElementDetails.fxml";

    @FXML
    private Node elementDetails;
    @FXML
    private Label coordinates;
    @FXML
    private Label typeValue;
    @FXML
    private Label stateValue;
    @FXML
    private Button breakButton;

    ElementDetails(Element element, ObservableIntegerValue time) {
        // this is executed first
        super(element, time, FXML_LOCATION);
        // this is executed AFTER initialize()
    }

    @FXML
    private void initialize() {
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

        breakButton.visibleProperty().bind(isRestSource);
        breakButton.setOnAction(event -> {
            breakButton.setDisable(true);
            RestSource dataSource = (RestSource) DataSourceHolder.getInstance().get();
            dataSource.breakElement(
                getObject(),
                () -> breakButton.setDisable(false)
            );
        });
    }

    @Nonnull
    @Override
    Node getDetails() {
        return elementDetails;
    }
}
