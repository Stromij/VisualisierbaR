package com.github.bachelorpraktikum.dbvisualization.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SwitchTest {

    private Context context;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        this.context = new Context();
    }

    private Switch createSwitch() {
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));

        Element element1 = Element.in(context).create("e1", Element.Type.WeichenPunktImpl, node1, Element.State.NOSIG);
        Element element2 = Element.in(context).create("e2", Element.Type.WeichenPunktImpl, node2, Element.State.FAHRT);
        Element element3 = Element.in(context).create("e3", Element.Type.WeichenPunktImpl, node3, Element.State.STOP);

        return element1.getSwitch().get();
    }

    @Test
    public void testAssociation() {
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));

        Element element1 = Element.in(context).create("e1", Element.Type.WeichenPunktImpl, node1, Element.State.NOSIG);
        Element element2 = Element.in(context).create("e2", Element.Type.WeichenPunktImpl, node2, Element.State.FAHRT);
        Element element3 = Element.in(context).create("e3", Element.Type.WeichenPunktImpl, node3, Element.State.STOP);

        assertTrue(element1.getSwitch().isPresent());
        assertTrue(element2.getSwitch().isPresent());
        assertTrue(element3.getSwitch().isPresent());

        Switch switch1 = element1.getSwitch().get();
        Switch switch2 = element2.getSwitch().get();
        Switch switch3 = element3.getSwitch().get();

        assertEquals(switch1, switch1);
        assertEquals(switch1, switch2);
        assertEquals(switch1, switch3);

        assertTrue(switch1.getElements().contains(element1));
        assertTrue(switch2.getElements().contains(element2));
        assertTrue(switch3.getElements().contains(element3));
    }

    @Test
    public void testOnlyThree() {
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));
        Node node4 = Node.in(context).create("n4", new Coordinates(3, 0));

        Element element1 = Element.in(context).create("e1", Element.Type.WeichenPunktImpl, node1, Element.State.NOSIG);
        Element element2 = Element.in(context).create("e2", Element.Type.WeichenPunktImpl, node2, Element.State.FAHRT);
        Element element3 = Element.in(context).create("e3", Element.Type.WeichenPunktImpl, node3, Element.State.STOP);
        Element element4 = Element.in(context).create("e4", Element.Type.WeichenPunktImpl, node4, Element.State.NOSIG);

        assertEquals(3, element1.getSwitch().get().getElements().size());
        assertEquals(3, element2.getSwitch().get().getElements().size());
        assertEquals(3, element3.getSwitch().get().getElements().size());
    }

    @Test
    public void testLessThanThree() {
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));

        Element element1 = Element.in(context).create("e1", Element.Type.WeichenPunktImpl, node1, Element.State.NOSIG);
        Element element2 = Element.in(context).create("e2", Element.Type.WeichenPunktImpl, node2, Element.State.FAHRT);

        Switch switch1 = element1.getSwitch().get();

        expected.expect(IllegalStateException.class);
        switch1.getElements();
    }
}
