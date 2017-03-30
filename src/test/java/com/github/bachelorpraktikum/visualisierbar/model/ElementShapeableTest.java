package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.model.Element.State;
import com.github.bachelorpraktikum.visualisierbar.model.Element.Type;

public class ElementShapeableTest extends ShapeableImplementationTest {

    @Override
    protected Shapeable<?> getShapeable() {
        Context context = new Context();
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        return Element.in(context).create("element", Type.HauptSignal, node, State.FAHRT);
    }
}
