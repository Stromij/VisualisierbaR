package com.github.bachelorpraktikum.visualisierbar.view.graph.elements;

import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.geometry.Point2D;
import javafx.scene.shape.Shape;

class StellwerkWechselElement extends PathElement {

    StellwerkWechselElement(Element element, Node node, CoordinatesAdapter adapter) {
        super(element, node, adapter);
    }

    @Override
    protected void resize(Shape shape) {
        super.resize(shape);
        shape.setScaleY(shape.getScaleY() * 1.5);
    }

    @Override
    protected Point2D getOffset() {
        return Point2D.ZERO;
    }
}
