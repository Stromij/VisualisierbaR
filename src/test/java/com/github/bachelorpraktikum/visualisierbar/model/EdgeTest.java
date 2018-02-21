package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.model.Edge.EdgeFactory;
import java.util.Random;

import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.SimpleCoordinatesAdapter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class EdgeTest extends FactoryTest<Edge> {

    private Context context;
    private Random random;
    private int nodeCounter;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        context = new Context();
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

    private Node getNode() {
        return getNode(context);
    }

    @Test
    public void testName() {
        Edge edge = Edge.in(context).create("Edge", 50, getNode(), getNode());
        assertEquals("Edge", edge.getName());
    }

    @Test
    public void testLength() {
        Edge edge = Edge.in(context).create("Edge", 50, getNode(), getNode());
        assertEquals(50, edge.getLength());
    }

    @Test
    public void testNode1() {
        Node node = getNode();
        Edge edge = Edge.in(context).create("Edge", 50, node, getNode());

        assertSame(edge.getNode1(), node);
    }

    @Test
    public void testNode2() {
        Node node = getNode();
        Edge edge = Edge.in(context).create("Edge", 50, getNode(), node);

        assertSame(edge.getNode2(), node);
    }

    @Test
    public void testToString() {
        Node n1 = getNode();
        Node n2 = getNode();
        Edge edge = Edge.in(context).create("Edge", 60, n1, n2);

        assertEquals(edge.toString(), "Edge{name='Edge', length=60, node1=" + n1.toString() +
            ", node2=" + n2.toString() + "}");
    }

    @Test
    public void testNullLength() {
        Integer length = null;

        expected.expect(NullPointerException.class);
        Edge edge = Edge.in(context).create("Edge", length, getNode(), getNode());
    }

    @Test
    public void testNullNodes() {
        expected.expect(IllegalArgumentException.class);
        Edge edge = Edge.in(context).create("Edge", 50, null, null);
    }

    @Test
    public void testNullName() {
        expected.expect(NullPointerException.class);
        Edge edge = Edge.in(context).create(null, 50, getNode(), getNode());
    }

    @Test
    public void testCreateNodeDifferentContext() {
        Node node1 = getNode();
        Node node2 = getNode();

        Context otherContext = new Context();
        expected.expect(IllegalArgumentException.class);
        Edge.in(otherContext).create("e", 1, node1, node2);
    }

    @Test
    public void testGetCommonNode() {
        Edge edge = createRandom(context);
        Node otherNode = getNode();
        Edge other1 = Edge.in(context).create("other1", 10, edge.getNode1(), otherNode);
        Edge other2 = Edge.in(context).create("other2", 10, otherNode, edge.getNode1());

        assertEquals(edge.getNode1(), edge.getCommonNode(other1));
        assertEquals(edge.getNode1(), edge.getCommonNode(other2));

        assertEquals(edge.getNode1(), other1.getCommonNode(edge));
        assertEquals(edge.getNode1(), other2.getCommonNode(edge));
    }

    @Test
    public void testGetCommonNodeInvalid() {
        Edge edge = createRandom(context);
        Edge other = createRandom(context);

        expected.expect(IllegalArgumentException.class);
        edge.getCommonNode(other);
    }

    @Test
    public void testGetCommonNodeNull() {
        Edge edge = createRandom(context);

        expected.expect(NullPointerException.class);
        edge.getCommonNode(null);
    }

    @Test
    public void testGetOtherNode() {
        Edge edge = createRandom(context);
        Node node1 = edge.getNode1();
        Node node2 = edge.getNode2();

        assertEquals(node2, edge.getOtherNode(node1));
        assertEquals(node1, edge.getOtherNode(node2));
    }

    @Test
    public void testGetOtherNodeInvalid() {
        Edge edge = createRandom(context);
        Node invalid = getNode();

        expected.expect(IllegalArgumentException.class);
        edge.getOtherNode(invalid);
    }

    @Test
    public void testGetOtherNodeNull() {
        Edge edge = createRandom(context);

        expected.expect(NullPointerException.class);
        edge.getOtherNode(null);
    }

    @Test
    public void testToABS()
        {Graph graph= new Graph(new Context(), new SimpleCoordinatesAdapter());

         Node n1 = Node.in(context).create("node1", new Coordinates(0, 0));
         Node n2 = Node.in(context).create("node2", new Coordinates(0, 0));
         Node n3 = Node.in(context).create("node3", new Coordinates(0, 0), "n3");
         Node n4 = Node.in(context).create("node4", new Coordinates(0, 0), "n4");

         Edge edge1 = Edge.in(context).create("edge1", 60, n1, n2);
         Edge edge2 = Edge.in(context).create("edge2", 90, n1, n3);
         Edge edge3 = Edge.in(context).create("edge3", 145, n3, n2);
         Edge edge4 = Edge.in(context).create("edge4", 153, n3, n4);

         Edge edge5 = Edge.in(context).create("edge5", 60, n1, n2, "e5");
         Edge edge6 = Edge.in(context).create("edge6", 90, n1, n3, "e6");
         Edge edge7 = Edge.in(context).create("edge7", 145, n3, n2, "e7");
         Edge edge8 = Edge.in(context).create("edge8", 153, n3, n4, "e8");

         String result1 = "[HTTPName: \"edge1\"]Edge edge1 = new local EdgeImpl(app,node1,node2,60,\"edge1\");\n";
         String result2 = "[HTTPName: \"edge2\"]Edge edge2 = new local EdgeImpl(app,node1,n3,90,\"edge2\");\n";
         String result3 = "[HTTPName: \"edge3\"]Edge edge3 = new local EdgeImpl(app,n3,node2,145,\"edge3\");\n";
         String result4 = "[HTTPName: \"edge4\"]Edge edge4 = new local EdgeImpl(app,n3,n4,153,\"edge4\");\n";

         String result5 = "[HTTPName: \"e5\"]Edge e5 = new local EdgeImpl(app,node1,node2,60,\"e5\");\n";
         String result6 = "[HTTPName: \"e6\"]Edge e6 = new local EdgeImpl(app,node1,n3,90,\"e6\");\n";
         String result7 = "[HTTPName: \"e7\"]Edge e7 = new local EdgeImpl(app,n3,node2,145,\"e7\");\n";
         String result8 = "[HTTPName: \"e8\"]Edge e8 = new local EdgeImpl(app,n3,n4,153,\"e8\");\n";

         assertEquals(result1, edge1.toABS());
         assertEquals(result2, edge2.toABS());
         assertEquals(result3, edge3.toABS());
         assertEquals(result4, edge4.toABS());
         assertEquals(result5, edge5.toABS());
         assertEquals(result6, edge6.toABS());
         assertEquals(result7, edge7.toABS());
         assertEquals(result8, edge8.toABS());
        }

    @Test
    public void testSetAbsName()
        {Graph graph= new Graph(new Context(), new SimpleCoordinatesAdapter());

         Node n1 = Node.in(context).create("node1", new Coordinates(0, 0));
         Node n2 = Node.in(context).create("node2", new Coordinates(10, 10));

         Edge edge1 = Edge.in(context).create("edge1", 60, n1, n2);
         Edge edge2 = Edge.in(context).create("edge2", 70, n2, n1);

         n1.setGraph(graph);
         n2.setGraph(graph);
         edge1.setGraph(graph);
         edge2.setGraph(graph);

         assertNull(edge1.getAbsName());
         assertNull(edge2.getAbsName());

         assertFalse(edge1.setAbsName(null));
         assertTrue(edge1.setAbsName("e1"));
         assertEquals("e1", edge1.getAbsName());

         assertFalse(edge2.setAbsName("e1"));
         assertTrue(edge2.setAbsName("e2"));
         assertEquals("e2", edge2.getAbsName());

         assertFalse(edge1.setAbsName("e2"));
         assertTrue(edge1.setAbsName("e3"));
         assertEquals("e3",edge1.getAbsName());

        }

    @Override
    protected EdgeFactory getFactory(Context context) {
        return Edge.in(context);
    }

    @Override
    protected Edge createRandom(Context context) {
        return getFactory(context)
            .create("edge" + nodeCounter++, random.nextInt(), getNode(context), getNode(context));
    }

    @Override
    protected Edge createSame(Context context, Edge edge) {
        return getFactory(context).create(
            edge.getName(),
            edge.getLength(),
            edge.getNode1(),
            edge.getNode2()
        );
    }

    @Override
    public void testCreateDifferentArg(Context context, Edge edge, int argIndex) {
        switch (argIndex) {
            case 1:
                getFactory(context).create(
                    edge.getName(),
                    edge.getLength() + 1,
                    edge.getNode1(),
                    edge.getNode2()
                );
                break;
            case 2:
                getFactory(context).create(
                    edge.getName(),
                    edge.getLength(),
                    getNode(context),
                    edge.getNode2()
                );
                break;
            case 3:
                getFactory(context).create(
                    edge.getName(),
                    edge.getLength(),
                    edge.getNode1(),
                    getNode(context)
                );
                break;
            case 4: throw new IllegalArgumentException();
            default:
                throw new IllegalStateException();
        }
    }
}
