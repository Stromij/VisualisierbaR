package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.Visualisierbar;
import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.SimpleCoordinatesAdapter;
import javafx.fxml.FXMLLoader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LogicalGroupTest {

    Context context;
    private int elementCounter;
    private int nodeCounter;
    private Random random;

    @Before
    public void init() {
        context = new Context();
        elementCounter = -1;
        nodeCounter = -1;
        random = new Random();
    }

    private Coordinates getCoordinate() {
        return new Coordinates(random.nextInt(Integer.MAX_VALUE),
                random.nextInt(Integer.MAX_VALUE));
    }

    private Node getNode(Context context) {
        nodeCounter++;
        return Node.in(context).create("node" + nodeCounter, getCoordinate());
    }

    private Element.Type getType() {
        switch(random.nextInt(9)){
            case 0: return Element.Type.VorSignal;
            case 1: return Element.Type.Magnet;
            case 2: return Element.Type.HauptSignal;
            case 3: return Element.Type.SichtbarkeitsPunkt;
            case 4: return Element.Type.SwWechsel;
            case 5: return Element.Type.GeschwindigkeitsVoranzeiger;
            case 6: return Element.Type.GeschwindigkeitsAnzeiger;
            case 7: return Element.Type.GefahrenPunkt;
            case 8: return Element.Type.WeichenPunkt;
            default: return Element.Type.UnknownElement;
        }
    }

    private Element getElement(Context context) {
        elementCounter++;
        return Element.in(context).create("node" + elementCounter, getType(), getNode(context), Element.State.NOSIG);
    }


    @Test
    public void testName() {
        LogicalGroup logGro = LogicalGroup.in(context).create("Group", LogicalGroup.Kind.DEFAULT);
        assertEquals("Group", logGro.getName());
    }

    @Test
    public void testKind() {
        LogicalGroup logGro1 = LogicalGroup.in(context).create("lg1", LogicalGroup.Kind.DEFAULT);
        assertEquals(LogicalGroup.Kind.DEFAULT, logGro1.getKind());
    }

    @Test
    public void testElements() {
        LinkedList<Element> list = new LinkedList<>();
        for(int i = 0; i < 10; i++) {list.add(getElement(context));}
        LogicalGroup logGro1 = LogicalGroup.in(context).create("lg2", LogicalGroup.Kind.DEFAULT, list);

        assertEquals(list, logGro1.getElements());
    }

    @Test
    public void testElement() {
        LinkedList<Element> list = new LinkedList<>();
        LogicalGroup logGro1 = LogicalGroup.in(context).create("lg3", LogicalGroup.Kind.DEFAULT);

        for(int i = 0; i < 10; i++) {
            Element elem = getElement(context);
            list.add(elem);
            logGro1.addElement(elem);
        }

        assertEquals(list, logGro1.getElements());
    }

    @Test
    public void testRemoveElement() {
        LogicalGroup logGro1 = LogicalGroup.in(context).create("lg4", LogicalGroup.Kind.DEFAULT);
        LinkedList<Element> list = new LinkedList<>();
        Element elem1 = getElement(context);
        Element elem2 = getElement(context);
        logGro1.addElement(elem1);
        logGro1.addElement(elem2);
        list.add(elem2);

        logGro1.removeElement(elem1);
        assertEquals(list, logGro1.getElements());

        logGro1.removeElement(elem2);
        assertEquals(new LinkedList<Element>(), logGro1.getElements());
    }

    @Test
    public void testToAbs1() throws IOException{
        String toFindArray[] = {
                "[HTTPName: \"s1\"]Signal s1 = new local SignalImpl(ss1, vs1, hs1, null, null, null, \"s1\", null);\n",
                "[HTTPName: \"s2\"]Signal s2 = new local SignalImpl(ss2, vs2, hs2, null, null, null, \"s2\", null);\n",
                "[HTTPName: \"s3\"]Signal s3 = new local SignalImpl(ss3, vs3, hs3, null, null, null, \"s3\", null);\n",
                "[HTTPName: \"s4\"]Signal s4 = new local SignalImpl(ss4, vs4, hs4, null, null, null, \"s4\", null);\n",
                "[HTTPName: \"s5\"]Signal s5 = new local SignalImpl(ss5, vs5, hs5, null, null, null, \"s5\", null);\n"
                };
        ArrayList<String> toFind = new ArrayList<>(Arrays.asList(toFindArray));
        int count = 0;

        Context context = new GraphParser().parse("src/test/resources/test10.zug.clean");

        for(LogicalGroup lg : LogicalGroup.in(context).getAll())
            {assertTrue(toFind.contains(lg.toABS()));
             count++;
            }
        assertEquals(count, toFind.size());

    }

}
