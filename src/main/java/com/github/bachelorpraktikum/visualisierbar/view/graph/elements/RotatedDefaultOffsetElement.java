package com.github.bachelorpraktikum.visualisierbar.view.graph.elements;

import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import javafx.scene.shape.Shape;

class RotatedDefaultOffsetElement extends DefaultOffsetElement {

    RotatedDefaultOffsetElement(Element element, Node node, CoordinatesAdapter adapter, int count) {
        super(element, node, adapter, count);
    }

    @Override
    protected void relocate(Shape shape) {
        super.relocate(shape);
        rotateAccordingToOffset(shape);
    }
}
