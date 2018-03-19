package com.github.bachelorpraktikum.visualisierbar.view.graph.elements;

import com.github.bachelorpraktikum.visualisierbar.model.Element;
import com.github.bachelorpraktikum.visualisierbar.model.Node;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import java.util.Collections;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;

abstract class SingleElementBase<S extends Shape> extends ElementBase<S> {

    SingleElementBase(Element element, Node node, CoordinatesAdapter adapter) {
        super(Collections.singletonList(element), node, adapter);
    }

    protected Element getElement() {
        return getRepresentedObjects().get(0);
    }

    @Nonnull
    @Override
    public Shape getShape(@Nonnull Element element) {
        return getShape();
    }
}
