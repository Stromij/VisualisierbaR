package com.github.bachelorpraktikum.visualisierbar.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Element.ElementFactory;
import com.github.bachelorpraktikum.visualisierbar.model.Element.State;
import com.github.bachelorpraktikum.visualisierbar.model.Element.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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

    @Test
    public void testToAbsSimpleWeichenpunkt() {
        Node nodeOfWp = Node.in(context).create("n10", new Coordinates(0, 0));
        Element wp = Element.in(context).create("w1_wa", Type.WeichenPunkt, nodeOfWp, State.NOSIG);
        String expect = "[HTTPName: \"w1_wa\"]WeichenPunkt w1_wa = new local WeichenPunktImpl(n10, \"w1_wa\");\nn10.addElement(w1_wa);\n";

        assertEquals(expect, wp.toABS(null));
    }

    @Test
    public void testToAbsSimpleHauptsignal() {
        Node nodeOfHs = Node.in(context).create("n34", new Coordinates(0, 0));
        Element hs = Element.in(context).create("hs3", Type.HauptSignal, nodeOfHs, State.NOSIG);
        String expect = "[HTTPName: \"hs3\"]HauptSignal hs3 = new local HauptSignalImpl(n34, null, \"hs3\");\nn34.addElement(hs3);\n";
        assertEquals(expect, hs.toABS(null));
    }

    @Test
    public void testToAbsSimpleGefahrenpunkt() {
        Node nodeOfGp = Node.in(context).create("n11", new Coordinates(0, 0));
        Element gp = Element.in(context).create("gp5", Type.GefahrenPunkt, nodeOfGp, State.NOSIG);
        String expect = "[HTTPName: \"gp5\"]GefahrenPunkt gp5 = new local GefahrenPunktImpl(null, \"gp5\");\nn11.addElement(gp5);\n";
        assertEquals(expect, gp.toABS(null));
    }

    @Test
    public void testToAbsSimpleGeschwindigkeitsanzeiger() {
        Node nodeOfGa = Node.in(context).create("n12", new Coordinates(0, 0));
        Element ga = Element.in(context).create("vs2", Type.GeschwindigkeitsAnzeiger, nodeOfGa, State.NOSIG);
        String expect = "[HTTPName: \"vs2\"]GeschwindigkeitsAnzeiger vs2 = new local GeschwindigkeitsAnzeigerImpl(null, \"vs2\");\nn12.addElement(vs2);\n";
        assertEquals(expect, ga.toABS(null));
    }

    @Test
    public void testToAbsSimpleGeschwindigkeitsvoranzeiger() {
        Node nodeOfGv = Node.in(context).create("n13", new Coordinates(0, 0));
        Element gv = Element.in(context).create("vs2", Type.GeschwindigkeitsVoranzeiger, nodeOfGv, State.NOSIG);
        String expect = "[HTTPName: \"vs2\"]GeschwindigkeitsVoranzeiger vs2 = new local GeschwindigkeitsVoranzeiger(null, \"vs2\");\nn13.addElement(vs2);\n";
        assertEquals(expect, gv.toABS(null));
    }

    @Test
    public void testToAbsSimpleSwWechsel() {
        Node nodeOfSw = Node.in(context).create("n14", new Coordinates(0, 0));
        Element sw = Element.in(context).create("ch5", Type.SwWechsel, nodeOfSw, State.NOSIG);
        String expect = "[HTTPName: \"ch5\"]SwWechsel ch5 = new SwWechselImpl(null, \"ch5\");\nn14.addElement(ch5);\n";
        assertEquals(expect, sw.toABS(null));
    }

    @Test
    public void testToAbsSimpleVorSignal() {
        Node nodeOfVs = Node.in(context).create("n15", new Coordinates(0, 0));
        Element vs = Element.in(context).create("vs2", Type.VorSignal, nodeOfVs, State.NOSIG);
        String expect = "[HTTPName: \"vs2\"]VorSignal vs2 = new local VorSignalImpl(null, \"vs2\");\nn15.addElement(vs2);\n";
        assertEquals(expect, vs.toABS(null));
    }

    @Test
    public void testToAbsSimplePZBMagnet() {
        Node nodeOfPm = Node.in(context).create("n16", new Coordinates(0, 0));
        Element pm = Element.in(context).create("PZBMagnetImpl", Type.Magnet, nodeOfPm, State.NOSIG);
        String expect = "[HTTPName: \"PZBMagnetImpl\"]Magnet PZBMagnetImpl = new local PZBMagnetImpl(null, null, \"PZBMagnetImpl\");\nn16.addElement(PZBMagnetImpl);\n";
        assertEquals(expect, pm.toABS(null));
    }

    @Test
    public void testToAbsSimpleContactMagnet() {
        Node nodeOfCm = Node.in(context).create("n17", new Coordinates(0, 0));
        Element cm = Element.in(context).create("ContactMagnetImpl", Type.Magnet, nodeOfCm, State.NOSIG);
        String expect = "[HTTPName: \"ContactMagnetImpl\"]ContactMagnet ContactMagnetImpl = new ContactMagnetImpl(\"ContactMagnetImpl\");\nn17.addElement(ContactMagnetImpl);\n";
        assertEquals(expect, cm.toABS(null));
    }

    @Test
    public void testToAbsSimpleDefaultMagnet() {
        Node nodeOfM = Node.in(context).create("n18", new Coordinates(0, 0));
        Element m = Element.in(context).create("m1", Type.Magnet, nodeOfM, State.NOSIG);
        String expect = "[HTTPName: \"m1\"]Magnet m1 = new MagnetImpl(\"m1\");\nn18.addElement(m1);\n";
        assertEquals(expect, m.toABS(null));
    }

    @Test
    public void testToAbsSimpleSichtbarkeitspunkt() {
        Node nodeOfSp = Node.in(context).create("n18", new Coordinates(0, 0));
        Element sp = Element.in(context).create("ss2", Type.SichtbarkeitsPunkt, nodeOfSp, State.NOSIG);
        String expect = "[HTTPName: \"ss2\"]SichtbarkeitsPunkt ss2 = new local SichtbarkeitsPunktImpl(null);\nn18.addElement(ss2);\n";
        assertEquals(expect, sp.toABS(null));
    }

    /**
     * Testet die ABS Ausgabe, wenn keine ABS-Namen vorhanden sind.
     * @throws IOException wenn Datei nicht lesbar
     */
    @Test
    public void testToAbsNoAbsNames() throws IOException
        {String toFindArray[] = {
                "[HTTPName: \"<0.99.0>:class_TrackElements_GefahrenPunktImpl\"]GefahrenPunkt <0.99.0>:class_TrackElements_GefahrenPunktImpl = new local GefahrenPunktImpl(null, \"<0.99.0>:class_TrackElements_GefahrenPunktImpl\");\n" + "<0.51.0>:class_Graph_NodeImpl.addElement(<0.99.0>:class_TrackElements_GefahrenPunktImpl);\n",
                "[HTTPName: \"<0.96.0>:class_TrackElements_HauptSignalImpl\"]HauptSignal <0.96.0>:class_TrackElements_HauptSignalImpl = new local HauptSignalImpl(<0.50.0>:class_Graph_NodeImpl, null, \"<0.96.0>:class_TrackElements_HauptSignalImpl\");\n" + "<0.50.0>:class_Graph_NodeImpl.addElement(<0.96.0>:class_TrackElements_HauptSignalImpl);\n",
                "[HTTPName: \"<0.97.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl\"]GeschwindigkeitsAnzeiger <0.97.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl = new local GeschwindigkeitsAnzeigerImpl(null, \"<0.97.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl\");\n" + "<0.50.0>:class_Graph_NodeImpl.addElement(<0.97.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl);\n",
                "[HTTPName: \"<0.102.0>:class_TrackElements_SwWechselImpl\"]SwWechsel <0.102.0>:class_TrackElements_SwWechselImpl = new SwWechselImpl(null, \"<0.102.0>:class_TrackElements_SwWechselImpl\");\n" + "<0.50.0>:class_Graph_NodeImpl.addElement(<0.102.0>:class_TrackElements_SwWechselImpl);\n",
                "[HTTPName: \"<0.94.0>:class_TrackElements_VorSignalImpl\"]VorSignal <0.94.0>:class_TrackElements_VorSignalImpl = new local VorSignalImpl(null, \"<0.94.0>:class_TrackElements_VorSignalImpl\");\n" + "<0.48.0>:class_Graph_NodeImpl.addElement(<0.94.0>:class_TrackElements_VorSignalImpl);\n",
                "[HTTPName: \"<0.93.0>:class_TrackElements_SichtbarkeitsPunktImpl\"]SichtbarkeitsPunkt <0.93.0>:class_TrackElements_SichtbarkeitsPunktImpl = new local SichtbarkeitsPunktImpl(null);\n" + "<0.47.0>:class_Graph_NodeImpl.addElement(<0.93.0>:class_TrackElements_SichtbarkeitsPunktImpl);\n",
                "[HTTPName: \"<0.118.0>:class_TrackElements_GefahrenPunktImpl\"]GefahrenPunkt <0.118.0>:class_TrackElements_GefahrenPunktImpl = new local GefahrenPunktImpl(null, \"<0.118.0>:class_TrackElements_GefahrenPunktImpl\");\n" + "<0.56.0>:class_Graph_NodeImpl.addElement(<0.118.0>:class_TrackElements_GefahrenPunktImpl);\n",
                "[HTTPName: \"<0.115.0>:class_TrackElements_HauptSignalImpl\"]HauptSignal <0.115.0>:class_TrackElements_HauptSignalImpl = new local HauptSignalImpl(<0.55.0>:class_Graph_NodeImpl, null, \"<0.115.0>:class_TrackElements_HauptSignalImpl\");\n" + "<0.55.0>:class_Graph_NodeImpl.addElement(<0.115.0>:class_TrackElements_HauptSignalImpl);\n",
                "[HTTPName: \"<0.116.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl\"]GeschwindigkeitsAnzeiger <0.116.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl = new local GeschwindigkeitsAnzeigerImpl(null, \"<0.116.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl\");\n" + "<0.55.0>:class_Graph_NodeImpl.addElement(<0.116.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl);\n",
                "[HTTPName: \"<0.113.0>:class_TrackElements_VorSignalImpl\"]VorSignal <0.113.0>:class_TrackElements_VorSignalImpl = new local VorSignalImpl(null, \"<0.113.0>:class_TrackElements_VorSignalImpl\");\n" + "<0.53.0>:class_Graph_NodeImpl.addElement(<0.113.0>:class_TrackElements_VorSignalImpl);\n",
                "[HTTPName: \"<0.112.0>:class_TrackElements_SichtbarkeitsPunktImpl\"]SichtbarkeitsPunkt <0.112.0>:class_TrackElements_SichtbarkeitsPunktImpl = new local SichtbarkeitsPunktImpl(null);\n" + "<0.52.0>:class_Graph_NodeImpl.addElement(<0.112.0>:class_TrackElements_SichtbarkeitsPunktImpl);\n",
                "[HTTPName: \"<0.125.0>:class_TrackElements_GefahrenPunktImpl\"]GefahrenPunkt <0.125.0>:class_TrackElements_GefahrenPunktImpl = new local GefahrenPunktImpl(null, \"<0.125.0>:class_TrackElements_GefahrenPunktImpl\");\n" + "<0.61.0>:class_Graph_NodeImpl.addElement(<0.125.0>:class_TrackElements_GefahrenPunktImpl);\n",
                "[HTTPName: \"<0.122.0>:class_TrackElements_HauptSignalImpl\"]HauptSignal <0.122.0>:class_TrackElements_HauptSignalImpl = new local HauptSignalImpl(<0.60.0>:class_Graph_NodeImpl, null, \"<0.122.0>:class_TrackElements_HauptSignalImpl\");\n" + "<0.60.0>:class_Graph_NodeImpl.addElement(<0.122.0>:class_TrackElements_HauptSignalImpl);\n",
                "[HTTPName: \"<0.123.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl\"]GeschwindigkeitsAnzeiger <0.123.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl = new local GeschwindigkeitsAnzeigerImpl(null, \"<0.123.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl\");\n" + "<0.60.0>:class_Graph_NodeImpl.addElement(<0.123.0>:class_TrackElements_GeschwindigkeitsAnzeigerImpl);\n",
                "[HTTPName: \"<0.120.0>:class_TrackElements_VorSignalImpl\"]VorSignal <0.120.0>:class_TrackElements_VorSignalImpl = new local VorSignalImpl(null, \"<0.120.0>:class_TrackElements_VorSignalImpl\");\n" + "<0.58.0>:class_Graph_NodeImpl.addElement(<0.120.0>:class_TrackElements_VorSignalImpl);\n",
                "[HTTPName: \"<0.119.0>:class_TrackElements_SichtbarkeitsPunktImpl\"]SichtbarkeitsPunkt <0.119.0>:class_TrackElements_SichtbarkeitsPunktImpl = new local SichtbarkeitsPunktImpl(null);\n" + "<0.57.0>:class_Graph_NodeImpl.addElement(<0.119.0>:class_TrackElements_SichtbarkeitsPunktImpl);\n",
                "[HTTPName: \"<0.95.0>:class_TrackElements_MagnetImpl\"]Magnet <0.95.0>:class_TrackElements_MagnetImpl = new MagnetImpl(\"<0.95.0>:class_TrackElements_MagnetImpl\");\n" + "<0.49.0>:class_Graph_NodeImpl.addElement(<0.95.0>:class_TrackElements_MagnetImpl);\n",
                "[HTTPName: \"<0.114.0>:class_TrackElements_MagnetImpl\"]Magnet <0.114.0>:class_TrackElements_MagnetImpl = new MagnetImpl(\"<0.114.0>:class_TrackElements_MagnetImpl\");\n" + "<0.54.0>:class_Graph_NodeImpl.addElement(<0.114.0>:class_TrackElements_MagnetImpl);\n",
                "[HTTPName: \"<0.121.0>:class_TrackElements_MagnetImpl\"]Magnet <0.121.0>:class_TrackElements_MagnetImpl = new MagnetImpl(\"<0.121.0>:class_TrackElements_MagnetImpl\");\n" + "<0.59.0>:class_Graph_NodeImpl.addElement(<0.121.0>:class_TrackElements_MagnetImpl);\n"
               };
        ArrayList<String> toFind = new ArrayList<>(Arrays.asList(toFindArray));
        int count = 0;

        Context context = new GraphParser().parse("src/test/resources/test5.zug.clean");
        for(Element elem : Element.in(context).getAll())
            {assertTrue(toFind.contains(elem.toABS(null)));
             count++;
            }
        assertTrue(count == toFindArray.length);
       }


    /**
     * Testet die ABS Ausgabe, wenn alle ABS-Namen vorhanden sind.
     * @throws IOException wenn Datei nicht lesbar
     */
    @Test
    public void testToAbsFullAbsNames() throws IOException
        {String toFindArray[] = {
                "[HTTPName: \"ss1\"]SichtbarkeitsPunkt ss1 = new local SichtbarkeitsPunktImpl(e55);\n" + "n11.addElement(ss1);\n",
                "[HTTPName: \"vs1\"]VorSignal vs1 = new local VorSignalImpl(e11, \"vs1\");\n" + "n12.addElement(vs1);\n",
                "[HTTPName: \"hs1\"]HauptSignal hs1 = new local HauptSignalImpl(n14, e13, \"hs1\");\n" + "n14.addElement(hs1);\n",
                "[HTTPName: \"ch1\"]SwWechsel ch1 = new SwWechselImpl(null, \"ch1\");\n" + "n14.addElement(ch1);\n",
                "[HTTPName: \"gp1\"]GefahrenPunkt gp1 = new local GefahrenPunktImpl(e14, \"gp1\");\n" + "n15.addElement(gp1);\n",
                "[HTTPName: \"ss2\"]SichtbarkeitsPunkt ss2 = new local SichtbarkeitsPunktImpl(e15);\n" + "n21.addElement(ss2);\n",
                "[HTTPName: \"vs2\"]VorSignal vs2 = new local VorSignalImpl(e21, \"vs2\");\n" + "n22.addElement(vs2);\n",
                "[HTTPName: \"hs2\"]HauptSignal hs2 = new local HauptSignalImpl(n24, e23, \"hs2\");\n" + "n24.addElement(hs2);\n",
                "[HTTPName: \"ch2\"]SwWechsel ch2 = new SwWechselImpl(null, \"ch2\");\n" + "n24.addElement(ch2);\n",
                "[HTTPName: \"gp2\"]GefahrenPunkt gp2 = new local GefahrenPunktImpl(e24, \"gp2\");\n" + "n25.addElement(gp2);\n",
                "[HTTPName: \"ss3\"]SichtbarkeitsPunkt ss3 = new local SichtbarkeitsPunktImpl(e25);\n" + "n31.addElement(ss3);\n",
                "[HTTPName: \"vs3\"]VorSignal vs3 = new local VorSignalImpl(e31, \"vs3\");\n" + "n32.addElement(vs3);\n",
                "[HTTPName: \"hs3\"]HauptSignal hs3 = new local HauptSignalImpl(n34, e33, \"hs3\");\n" + "n34.addElement(hs3);\n",
                "[HTTPName: \"ch3\"]SwWechsel ch3 = new SwWechselImpl(null, \"ch3\");\n" + "n34.addElement(ch3);\n",
                "[HTTPName: \"gp3\"]GefahrenPunkt gp3 = new local GefahrenPunktImpl(e34, \"gp3\");\n" + "n35.addElement(gp3);\n",
                "[HTTPName: \"ss4\"]SichtbarkeitsPunkt ss4 = new local SichtbarkeitsPunktImpl(e35);\n" + "n41.addElement(ss4);\n",
                "[HTTPName: \"vs4\"]VorSignal vs4 = new local VorSignalImpl(e41, \"vs4\");\n" + "n42.addElement(vs4);\n",
                "[HTTPName: \"hs4\"]HauptSignal hs4 = new local HauptSignalImpl(n44, e43, \"hs4\");\n" + "n44.addElement(hs4);\n",
                "[HTTPName: \"ch4\"]SwWechsel ch4 = new SwWechselImpl(null, \"ch4\");\n" + "n44.addElement(ch4);\n",
                "[HTTPName: \"gp4\"]GefahrenPunkt gp4 = new local GefahrenPunktImpl(e44, \"gp4\");\n" + "n45.addElement(gp4);\n",
                "[HTTPName: \"ss5\"]SichtbarkeitsPunkt ss5 = new local SichtbarkeitsPunktImpl(e45);\n" + "n51.addElement(ss5);\n",
                "[HTTPName: \"vs5\"]VorSignal vs5 = new local VorSignalImpl(e51, \"vs5\");\n" + "n52.addElement(vs5);\n",
                "[HTTPName: \"hs5\"]HauptSignal hs5 = new local HauptSignalImpl(n54, e53, \"hs5\");\n" + "n54.addElement(hs5);\n",
                "[HTTPName: \"ch5\"]SwWechsel ch5 = new SwWechselImpl(null, \"ch5\");\n" + "n54.addElement(ch5);\n",
                "[HTTPName: \"gp5\"]GefahrenPunkt gp5 = new local GefahrenPunktImpl(e54, \"gp5\");\n" + "n55.addElement(gp5);\n"
               };
         ArrayList<String> toFind = new ArrayList<>(Arrays.asList(toFindArray));
         int count = 0;

         Context context = new GraphParser().parse("src/test/resources/test10.zug.clean");
         for(Element elem : Element.in(context).getAll())
            {assertTrue(toFind.contains(elem.toABS(null)));
             count++;
            }
         assertTrue(count == toFindArray.length);
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
