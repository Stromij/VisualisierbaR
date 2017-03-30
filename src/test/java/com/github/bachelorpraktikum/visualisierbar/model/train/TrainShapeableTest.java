package com.github.bachelorpraktikum.visualisierbar.model.train;

import com.github.bachelorpraktikum.visualisierbar.model.Context;
import com.github.bachelorpraktikum.visualisierbar.model.Shapeable;
import com.github.bachelorpraktikum.visualisierbar.model.ShapeableImplementationTest;

public class TrainShapeableTest extends ShapeableImplementationTest {

    @Override
    protected Shapeable<?> getShapeable() {
        Context context = new Context();
        return Train.in(context).create("train", "t", 10);
    }
}
