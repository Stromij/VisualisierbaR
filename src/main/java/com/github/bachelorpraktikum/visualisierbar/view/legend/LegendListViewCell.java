package com.github.bachelorpraktikum.visualisierbar.view.legend;

import com.github.bachelorpraktikum.visualisierbar.model.Shapeable;
import com.github.bachelorpraktikum.visualisierbar.model.Shapeable.VisibleState;
import com.github.bachelorpraktikum.visualisierbar.view.DataSourceHolder;
import java.io.IOException;
import javafx.beans.binding.Binding;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class LegendListViewCell extends ListCell<Shapeable<?>> {

    @FXML
    private Label eleName;
    @FXML
    private Group eleImage;
    @FXML
    private CheckBox checkbox;
    @FXML
    private AnchorPane cell;

    @Override
    protected void updateItem(Shapeable<?> element, boolean empty) {
        super.updateItem(element, empty);
        if (empty) {
            setText(null);
            Rectangle emptyRect = new Rectangle();
            emptyRect.setOpacity(1);
            setGraphic(emptyRect);
        } else {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("legendCell.fxml"));
            loader.setController(this);
            try {
                Node listCell = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String name = element.getName();
            Shape shape = element.createIconShape();

            resizeShape(shape, 20);

            eleName.setText(name);
            eleImage.getChildren().clear();
            eleImage.getChildren().add(shape);
            setGraphic(cell);

            Binding<VisibleState> binding = Bindings.createObjectBinding(() -> {
                if (checkbox.isIndeterminate()) {
                    return VisibleState.AUTO;
                } else if (checkbox.isSelected()) {
                    return VisibleState.ENABLED;
                } else {
                    return VisibleState.DISABLED;
                }
            }, checkbox.selectedProperty(), checkbox.indeterminateProperty());
            DataSourceHolder.getInstance().getContext().addObject(binding);
            element.visibleStateProperty().bind(binding);
        }
    }

    /**
     * resize a shape
     * @param shape that be resized
     * @param max max
     */
    private void resizeShape(Shape shape, double max) {
        Bounds shapeBounds = shape.getBoundsInParent();
        double maxShape = Math.max(shapeBounds.getWidth(), shapeBounds.getHeight());
        double factor = max / maxShape;
        shape.setScaleX(shape.getScaleX() * factor);
        shape.setScaleY(shape.getScaleY() * factor);
    }
}
