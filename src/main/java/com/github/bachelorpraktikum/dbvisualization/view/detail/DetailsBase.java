package com.github.bachelorpraktikum.dbvisualization.view.detail;

import com.github.bachelorpraktikum.dbvisualization.model.Element;
import com.github.bachelorpraktikum.dbvisualization.model.GraphObject;
import com.github.bachelorpraktikum.dbvisualization.model.train.Train;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;

public abstract class DetailsBase<O extends GraphObject<?>> {

    private final O element;
    private final ObservableIntegerValue time;
    private final List<Object> bindings;

    DetailsBase(O element, ObservableIntegerValue time, String fxmlLocation) {
        this.element = element;
        this.time = time;
        this.bindings = new LinkedList<>();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlLocation));
        loader.setResources(ResourceBundle.getBundle("bundles.localization"));
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
            throw new UncheckedIOException(e);
        }
    }

    void addBinding(Object binding) {
        bindings.add(binding);
    }

    O getObject() {
        return element;
    }

    String getName() {
        return getObject().getReadableName();
    }

    @Nonnull
    abstract Node getDetails();

    String getCoordinatesString(Point2D coord) {
        if (coord == null) {
            return ResourceBundle.getBundle("bundles.localization").getString("unavailable");
        } else {
            return String.format("x: %f | y: %f", coord.getX(), coord.getY());
        }
    }

    ObservableIntegerValue timeProperty() {
        return time;
    }

    protected Shape getShape() {
        return getObject().createIconShape();
    }

    public static DetailsBase create(GraphObject<?> object, ObservableIntegerValue timeProperty,
        Pane centerPane) {
        if (object instanceof Train) {
            Train train = (Train) object;
            return new TrainDetails(train, timeProperty, centerPane);
        } else if (object instanceof Element) {
            Element element = (Element) object;
            return new ElementDetails(element, timeProperty);
        } else {
            throw new IllegalArgumentException();
        }
    }
}
