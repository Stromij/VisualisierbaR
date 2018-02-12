package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.model.Node.NodeFactory;
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
    public void testToABS()
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
            default:
                throw new IllegalStateException();
        }
    }
}
