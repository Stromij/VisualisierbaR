package com.github.bachelorpraktikum.visualisierbar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.bachelorpraktikum.visualisierbar.model.Element.ElementFactory;
import com.github.bachelorpraktikum.visualisierbar.model.Element.State;
import com.github.bachelorpraktikum.visualisierbar.model.Element.Type;
import java.util.concurrent.atomic.AtomicReference;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ElementTest extends FactoryTest<Element> {

    private Context context;
    private int counter;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        this.context = new Context();
    }

    private Node createNode(Context context) {
        Coordinates coordinates = new Coordinates(counter++, counter++);
        return Node.in(context).create("node" + counter++, coordinates);
    }

    private Node createNode() {
        return createNode(context);
    }

    private Element createElement(Context context) {
        Node node = createNode(context);
        return Element.in(context)
            .create("element" + counter++, Element.Type.HauptSignal, node, Element.State.NOSIG);
    }

    private Element createElement() {
        return createElement(context);
    }

    @Test
    public void testInitialState() {
        Node node = Node.in(context).create("node", new Coordinates(10, 10));

        Element.State initState = Element.State.FAHRT;
        Element element = Element.in(context)
            .create("e", Element.Type.GefahrenPunkt, node, initState);

        Element.State otherState = Element.State.STOP;
        int otherTime = 20;
        element.addEvent(otherState, otherTime);

        assertEquals(initState, element.getState());

        Element.in(context).setTime(10);
        assertEquals(initState, element.getState());

        Element.in(context).setTime(30);
        assertEquals(otherState, element.getState());
        Element.in(context).setTime(0);
        assertEquals(initState, element.getState());
    }

    @Test
    public void testAddEvent() {
        Element element = createElement();

        int time = 1;
        for (Element.State state : Element.State.values()) {
            element.addEvent(state, time);

            Element.in(context).setTime(time);
            assertEquals(state, element.getState());

            Element.in(context).setTime(time + 1);
            assertEquals(state, element.getState());

            Element.in(context).setTime(0);
            time += 1;
        }
    }

    @Test
    public void testAddTwoEventsSameTime() {
        Element element = createElement();

        int time = 20;

        element.addEvent(Element.State.FAHRT, time);
        element.addEvent(Element.State.STOP, time);

        Element.in(context).setTime(time);
        assertEquals(Element.State.STOP, element.getState());
    }

    @Test
    public void testAddEventCurrentTime() {
        Element element = createElement();

        Element.State initState = Element.State.STOP;
        Element.State newState = Element.State.FAHRT;

        element.addEvent(initState, 1);
        Element.in(context).setTime(2);
        assertEquals(initState, element.getState());

        // Since the current time of all events is 1,
        // the element should be updated without a call to Element.in(context).setTime()
        element.addEvent(newState, 2);
        assertEquals(newState, element.getState());
    }

    @Test
    public void testAddEventNegativeTime() {
        Element element = createElement();

        // Should get corrected to time 0
        element.addEvent(Element.State.FAHRT, -1);
        boolean hasZeroTime = false;
        boolean zeroTimeHasWarnings = false;
        for (Event event : Element.in(context).getEvents()) {
            assertFalse(event.getTime() < Context.INIT_STATE_TIME);
            if (event.getTime() == 0) {
                hasZeroTime = true;
                if (!event.getWarnings().isEmpty()) {
                    zeroTimeHasWarnings = true;
                }
            }
        }
        assertTrue(hasZeroTime);
        assertTrue(zeroTimeHasWarnings);
    }

    @Test
    public void testAddEventTwice() {
        Element element = createElement();

        element.addEvent(State.FAHRT, 10);
        element.addEvent(State.FAHRT, 10);

        Event event = getFactory(context).getEvents().get(1);
        Event event2 = getFactory(context).getEvents().get(2);

        assertFalse(event.equals(null));

        assertEquals(event, event);

        assertNotSame(event, event2);
        assertEquals(event, event2);
        assertEquals(event2, event);

        assertEquals(event.hashCode(), event2.hashCode());
    }

    @Test
    public void testEventEqualDifferentElements() {
        Element element1 = createElement();
        Element element2 = createElement();
        element2.addEvent(State.FAHRT, 10);

        Event event1 = getFactory(context).getEvents().get(0);
        Event event2 = getFactory(context).getEvents().get(1);
        Event event3 = getFactory(context).getEvents().get(2);

        assertNotSame(event1, event2);

        assertNotEquals(event1, event2);
        assertNotEquals(event2, event1);

        assertNotEquals(event2, event3);
        assertNotEquals(event1, event3);
    }

    @Test
    public void testAddEventPast() {
        Element element = createElement();
        element.addEvent(Element.State.FAHRT, 10);

        // Expected to be added at time 10 with warning
        element.addEvent(Element.State.STOP, 9);
        // the index of the new event is 2, because the init event has index 0
        Event event = Element.in(context).getEvents().get(2);
        assertEquals(10, event.getTime());
        assertFalse(event.getWarnings().isEmpty());
    }

    @Test
    public void testAddEventNull() {
        Element element = createElement();

        expected.expect(NullPointerException.class);
        element.addEvent(null, 10);
    }

    @Test
    public void testGetName() {
        Element element = getFactory(context)
            .create("element", Type.GefahrenPunkt, createNode(), State.NOSIG);
        assertEquals("element", element.getName());
    }

    @Test
    public void testGetNode() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        Element element = Element.in(context)
            .create("element", Element.Type.HauptSignal, node, Element.State.NOSIG);

        assertEquals(node, element.getNode());
    }

    @Test
    public void testGetState() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateProperty = element.stateProperty();

        assertEquals(stateProperty.getValue(), element.getState());

        element.addEvent(Element.State.FAHRT, 10);
        Element.in(context).setTime(10);

        assertEquals(Element.State.FAHRT, element.getState());
    }

    @Test
    public void testGetSwitch() {
        Node node = createNode();
        Element e1 = Element.in(context).create("e1", Type.WeichenPunkt, node, State.NOSIG);
        Element e2 = Element.in(context).create("e2", Type.WeichenPunkt, node, State.NOSIG);
        Element e3 = Element.in(context).create("e3", Type.WeichenPunkt, node, State.NOSIG);

        assertEquals(e1.getSwitch(), e2.getSwitch());
        assertEquals(e2.getSwitch(), e3.getSwitch());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetSwitchNoSwitch() {
        createElement().getSwitch();
    }

    @Test
    public void testStatePropertyCastToWritable() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateReadOnlyProperty = element.stateProperty();

        expected.expect(ClassCastException.class);
        Property<Element.State> property = (Property<Element.State>) stateReadOnlyProperty;
    }

    @Test
    public void testStatePropertySame() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateProperty = element.stateProperty();

        element.addEvent(Element.State.FAHRT, 100);
        Element.in(context).setTime(10);
        Element.in(context).setTime(200);

        assertSame(stateProperty, element.stateProperty());
    }

    @Test
    public void testStatePropertyCalled() {
        Element element = createElement();
        ReadOnlyProperty<Element.State> stateProperty = element.stateProperty();

        AtomicReference<Element.State> called = new AtomicReference<>();
        stateProperty.addListener((observable, oldValue, newValue) -> called.set(newValue));

        Element.State state = Element.State.FAHRT;
        element.addEvent(state, 10);

        Element.in(context).setTime(10);
        assertEquals(state, called.get());
    }

    @Test
    public void testGetType() {
        Element element = createElement();
        assertEquals(Element.Type.HauptSignal, element.getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateNodeDifferentContext() {
        Node node = createNode(context);
        Context otherContext = new Context();
        Element.in(otherContext).create("e", Type.HauptSignal, node, State.FAHRT);
    }

    @Test
    public void testSetTimeInitialTime() {
        Element.in(context).setTime(Context.INIT_STATE_TIME);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetTimeInvalidTime() {
        Element.in(context).setTime(-2);
    }

    @Override
    protected ElementFactory getFactory(Context context) {
        return Element.in(context);
    }

    @Override
    protected Element createRandom(Context context) {
        return createElement(context);
    }

    @Override
    protected Element createSame(Context context, Element element) {
        return getFactory(context).create(
            element.getName(),
            element.getType(),
            element.getNode(),
            element.getState()
        );
    }

    @Override
    public void testCreateDifferentArg(Context context, Element element, int argIndex) {
        switch (argIndex) {
            case 1:
                Element.Type[] values = Element.Type.values();
                int newIndex = (element.getType().ordinal() + 1) % values.length;
                Element.Type type = values[newIndex];
                getFactory(context)
                    .create(element.getName(), type, element.getNode(), element.getState());
                break;
            case 2:
                getFactory(context)
                    .create(element.getName(), element.getType(), createNode(context),
                        element.getState());
                break;
            case 3:
                getFactory(context)
                    .create(element.getName(), element.getType(), element.getNode(), State.FAHRT);
                break;
            default:
                throw new IllegalStateException();
        }
    }

    @Test
    public void testLogicalGroup(){
        Element element = createElement();
        LogicalGroup group = LogicalGroup.in(context).create("Test", LogicalGroup.Kind.DEFAULT);
        group.addElement(element);
        assertTrue(group.getElements().contains(element));
        assertTrue(element.getLogicalGroup()==group);
        group.removeElement(element);
        assertFalse(group.getElements().contains(element));
        assertTrue(element.getLogicalGroup()==null);
    }

}
