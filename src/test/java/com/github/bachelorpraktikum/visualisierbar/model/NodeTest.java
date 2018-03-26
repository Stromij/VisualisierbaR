package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Node.NodeFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.SimpleCoordinatesAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class NodeTest extends FactoryTest<Node> {

    private Context context;
    private int counter = 0;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        context = new Context();
    }

    @Test
    public void testName() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        assertEquals("node", node.getName());
    }

    @Test
    public void testCoordinates() {
        Coordinates coords = new Coordinates(0, 0);
        Node node = Node.in(context).create("node", coords);
        assertEquals(coords, node.getCoordinates());
    }

    @Test
    public void testToString() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        assertNotNull(node.toString());
        assertFalse(node.toString().trim().isEmpty());
    }

    @Test
    public void testNullCoordinates() {
        expected.expect(NullPointerException.class);
        Node.in(context).create("node", null);
    }

    @Test
    public void testNullName() {
        expected.expect(NullPointerException.class);
        Node.in(context).create(null, new Coordinates(0, 0));
    }

    @Test
    public void testAddEdge() {
        Node node1 = Node.in(context).create("node1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("node2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("edge", 10, node1, node2);

        Node testNode = Node.in(context).create("testNode", new Coordinates(10, 0));
        testNode.addEdge(edge);
        assertTrue(testNode.getEdges().contains(edge));
    }

    @Test
    public void testAddElement() {
        Node node = Node.in(context).create("node", new Coordinates(0, 0));
        Element element = Element.in(context)
            .create("element", Element.Type.HauptSignal, node, Element.State.NOSIG);

        Node testNode = Node.in(context).create("testNode", new Coordinates(10, 0));
        testNode.addElement(element);
        assertTrue(testNode.getElements().contains(element));
    }

    @Test
    public void testOtherEdges() {
        Node node1 = Node.in(context).create("node1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("node2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("node3", new Coordinates(2, 1));

        Edge edge1 = Edge.in(context).create("edge1", 10, node1, node2);
        Edge edge2 = Edge.in(context).create("edge2", 10, node2, node3);
        Edge edge3 = Edge.in(context).create("edge3", 10, node1, node3);

        Collection<Edge> otherEdges = node1.otherEdges(edge1);
        assertEquals(1, otherEdges.size());
        assertFalse(otherEdges.contains(edge1));
        assertTrue(otherEdges.contains(edge3));
    }

    @Test
    public void testOtherEdgesOnlyEdge() {
        Node node1 = Node.in(context).create("node1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("node2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("edge", 10, node1, node2);

        Collection<Edge> otherEdges = node1.otherEdges(edge);

        assertTrue(otherEdges.isEmpty());
    }

    @Test
    public void testOtherEdgesNull() {
        Node node1 = Node.in(context).create("node1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("node2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("edge", 10, node1, node2);

        expected.expect(NullPointerException.class);
        node1.otherEdges(null);
    }

    @Test
    public void testToABSSimple()
        {Node node0 = Node.in(context).create("node0", new Coordinates(0, 0));
         Node node1 = Node.in(context).create("node1", new Coordinates(3, 6));
         Node node2 = Node.in(context).create("node2", new Coordinates(4, 5), "n2");
         Node node3 = Node.in(context).create("node3", new Coordinates(8, 12), "n3");

         String result0 = "[HTTPName: \"node0\"]Node node0 = new local NodeImpl(0,0,\"node0\");\n";
         String result1 = "[HTTPName: \"node1\"]Node node1 = new local NodeImpl(3,6,\"node1\");\n";
         String result2 = "[HTTPName: \"n2\"]Node n2 = new local NodeImpl(4,5,\"n2\");\n";
         String result3 = "[HTTPName: \"n3\"]Node n3 = new local NodeImpl(8,12,\"n3\");\n";

         assertEquals(result0, node0.toABS());
         assertEquals(result1, node1.toABS());
         assertEquals(result2, node2.toABS());
         assertEquals(result3, node3.toABS());
        }

    /**
     * Testet die ABS Ausgabe, wenn keine ABS-Namen vorhanden sind.
     * @throws IOException wenn Datei nicht lesbar
     */
    @Test
    public void testToAbsNoAbsNames() throws IOException
        {String toFindArray[] = {
                "[HTTPName: \"<0.45.0>:class_Graph_NodeImpl\"]Node <0.45.0>:class_Graph_NodeImpl = new local NodeImpl(0,0,\"<0.45.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.46.0>:class_Graph_NodeImpl\"]Node <0.46.0>:class_Graph_NodeImpl = new local NodeImpl(12,8,\"<0.46.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.47.0>:class_Graph_NodeImpl\"]Node <0.47.0>:class_Graph_NodeImpl = new local NodeImpl(4,0,\"<0.47.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.48.0>:class_Graph_NodeImpl\"]Node <0.48.0>:class_Graph_NodeImpl = new local NodeImpl(5,0,\"<0.48.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.49.0>:class_Graph_NodeImpl\"]Node <0.49.0>:class_Graph_NodeImpl = new local NodeImpl(6,0,\"<0.49.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.50.0>:class_Graph_NodeImpl\"]Node <0.50.0>:class_Graph_NodeImpl = new local NodeImpl(7,0,\"<0.50.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.51.0>:class_Graph_NodeImpl\"]Node <0.51.0>:class_Graph_NodeImpl = new local NodeImpl(8,1,\"<0.51.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.52.0>:class_Graph_NodeImpl\"]Node <0.52.0>:class_Graph_NodeImpl = new local NodeImpl(8,3,\"<0.52.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.53.0>:class_Graph_NodeImpl\"]Node <0.53.0>:class_Graph_NodeImpl = new local NodeImpl(7,4,\"<0.53.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.54.0>:class_Graph_NodeImpl\"]Node <0.54.0>:class_Graph_NodeImpl = new local NodeImpl(6,4,\"<0.54.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.55.0>:class_Graph_NodeImpl\"]Node <0.55.0>:class_Graph_NodeImpl = new local NodeImpl(5,4,\"<0.55.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.56.0>:class_Graph_NodeImpl\"]Node <0.56.0>:class_Graph_NodeImpl = new local NodeImpl(4,5,\"<0.56.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.57.0>:class_Graph_NodeImpl\"]Node <0.57.0>:class_Graph_NodeImpl = new local NodeImpl(4,7,\"<0.57.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.58.0>:class_Graph_NodeImpl\"]Node <0.58.0>:class_Graph_NodeImpl = new local NodeImpl(5,8,\"<0.58.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.59.0>:class_Graph_NodeImpl\"]Node <0.59.0>:class_Graph_NodeImpl = new local NodeImpl(6,8,\"<0.59.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.60.0>:class_Graph_NodeImpl\"]Node <0.60.0>:class_Graph_NodeImpl = new local NodeImpl(7,8,\"<0.60.0>:class_Graph_NodeImpl\");\n",
                "[HTTPName: \"<0.61.0>:class_Graph_NodeImpl\"]Node <0.61.0>:class_Graph_NodeImpl = new local NodeImpl(8,8,\"<0.61.0>:class_Graph_NodeImpl\");\n",

        };
         ArrayList<String> toFind = new ArrayList<>(Arrays.asList(toFindArray));
         int count = 0;

         Context context = new GraphParser().parse("src/test/resources/test5.zug.clean");
         for(Node node : Node.in(context).getAll())
            {assertTrue(toFind.contains(node.toABS()));
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
                "[HTTPName: \"n01\"]Node n01 = new local NodeImpl(0,0,\"n01\");\n",
                "[HTTPName: \"n02\"]Node n02 = new local NodeImpl(2,0,\"n02\");\n",
                "[HTTPName: \"n03\"]Node n03 = new local NodeImpl(3,0,\"n03\");\n",
                "[HTTPName: \"n04\"]Node n04 = new local NodeImpl(4,0,\"n04\");\n",
                "[HTTPName: \"n05\"]Node n05 = new local NodeImpl(5,0,\"n05\");\n",
                "[HTTPName: \"n06\"]Node n06 = new local NodeImpl(6,0,\"n06\");\n",
                "[HTTPName: \"n07\"]Node n07 = new local NodeImpl(8,0,\"n07\");\n",
                "[HTTPName: \"n08\"]Node n08 = new local NodeImpl(9,0,\"n08\");\n",
                "[HTTPName: \"n09\"]Node n09 = new local NodeImpl(10,0,\"n09\");\n",
                "[HTTPName: \"n10\"]Node n10 = new local NodeImpl(11,0,\"n10\");\n",
                "[HTTPName: \"n11\"]Node n11 = new local NodeImpl(12,0,\"n11\");\n",
                "[HTTPName: \"n12\"]Node n12 = new local NodeImpl(13,0,\"n12\");\n",
                "[HTTPName: \"n13\"]Node n13 = new local NodeImpl(12,1,\"n13\");\n",
                "[HTTPName: \"n14\"]Node n14 = new local NodeImpl(13,1,\"n14\");\n"
               };
         ArrayList<String> toFind = new ArrayList<>(Arrays.asList(toFindArray));
         int count = 0;

         Context context = new GraphParser().parse("src/test/resources/test9.zug.clean");
         for(Node node : Node.in(context).getAll())
            {assertTrue(toFind.contains(node.toABS()));
             count++;
            }
         assertTrue(count == toFindArray.length);
        }

    @Test
    public void testSetAbsName()
        {Graph graph= new Graph(new Context(), new SimpleCoordinatesAdapter());

         Node node0 = Node.in(graph.getContext()).create("node0", new Coordinates(0, 0));
         Node node1 = Node.in(graph.getContext()).create("node1", new Coordinates(3, 6));

         node0.setGraph(graph);
         node1.setGraph(graph);

         assertNull(node0.getAbsName());
         assertNull(node1.getAbsName());

         assertFalse(node0.setAbsName(null));
         assertTrue(node0.setAbsName("n1"));
         assertEquals("n1", node0.getAbsName());

         assertFalse(node1.setAbsName("n1"));
         assertTrue(node1.setAbsName("n2"));
         assertEquals("n2", node1.getAbsName());

         assertFalse(node0.setAbsName("n2"));
         assertTrue(node0.setAbsName("n3"));
         assertEquals("n3", node0.getAbsName());
        }

    @Override
    protected NodeFactory getFactory(Context context) {
        return Node.in(context);
    }

    private Coordinates createCoordinates() {
        return new Coordinates(counter++, counter++);
    }

    @Override
    protected Node createRandom(Context context) {
        return getFactory(context).create("node" + counter++, createCoordinates());
    }

    @Override
    protected Node createSame(Context context, Node node) {
        return getFactory(context).create(node.getName(), node.getCoordinates());
    }

    @Override
    public void testCreateDifferentArg(Context context, Node node, int argIndex) {
        switch (argIndex) {
            case 1:
                getFactory(context).create(node.getName(), createCoordinates());
                break;
            case 2:
                getFactory(context).create(node.getName(), createCoordinates(),"gg");
                break;
            default:
                throw new IllegalStateException();
        }
    }
}
