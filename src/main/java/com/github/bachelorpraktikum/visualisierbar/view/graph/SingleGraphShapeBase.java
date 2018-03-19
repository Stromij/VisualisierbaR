package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.GraphObject;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import java.util.Collections;
import java.util.List;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;

public abstract class SingleGraphShapeBase<R extends GraphObject<?>, S extends Shape>
    extends GraphShapeBase<R, S> {

    private final R represented;

    SingleGraphShapeBase(R represented, CoordinatesAdapter adapter) {
        super(adapter);
        this.represented = represented;
    }

    protected final R getRepresented() {
        return represented;
    }

    @Nonnull
    @Override
    public List<R> getRepresentedObjects() {
        return Collections.singletonList(represented);
    }

    @Nonnull
    @Override
    public final Shape getShape(@Nonnull R represented) {
        return getShape();
    }
}
