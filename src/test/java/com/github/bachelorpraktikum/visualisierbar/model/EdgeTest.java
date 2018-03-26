package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.logparser.GraphParser;
import com.github.bachelorpraktikum.visualisierbar.model.Edge.EdgeFactory;

import java.io.IOException;
import java.util.*;

import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import com.github.bachelorpraktikum.visualisierbar.view.graph.GraphShape;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.SimpleCoordinatesAdapter;
import javafx.geometry.Point2D;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Nonnull;

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
    public void testToABSSimple()
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

    /**
     * Testet die ABS Ausgabe, wenn keine ABS-Namen vorhanden sind.
     * @throws IOException wenn Datei nicht lesbar
     */
    @Test
    public void testToAbsNoAbsNames() throws IOException
        {String toFindArray[] = {
                "[HTTPName: \"<0.62.0>:class_Graph_EdgeImpl\"]Edge <0.62.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.45.0>:class_Graph_NodeImpl,<0.47.0>:class_Graph_NodeImpl,4000,\"<0.62.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.63.0>:class_Graph_EdgeImpl\"]Edge <0.63.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.47.0>:class_Graph_NodeImpl,<0.48.0>:class_Graph_NodeImpl,275,\"<0.63.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.64.0>:class_Graph_EdgeImpl\"]Edge <0.64.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.48.0>:class_Graph_NodeImpl,<0.49.0>:class_Graph_NodeImpl,750,\"<0.64.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.65.0>:class_Graph_EdgeImpl\"]Edge <0.65.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.49.0>:class_Graph_NodeImpl,<0.50.0>:class_Graph_NodeImpl,250,\"<0.65.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.66.0>:class_Graph_EdgeImpl\"]Edge <0.66.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.50.0>:class_Graph_NodeImpl,<0.51.0>:class_Graph_NodeImpl,100,\"<0.66.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.67.0>:class_Graph_EdgeImpl\"]Edge <0.67.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.51.0>:class_Graph_NodeImpl,<0.52.0>:class_Graph_NodeImpl,4000,\"<0.67.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.68.0>:class_Graph_EdgeImpl\"]Edge <0.68.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.52.0>:class_Graph_NodeImpl,<0.53.0>:class_Graph_NodeImpl,275,\"<0.68.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.69.0>:class_Graph_EdgeImpl\"]Edge <0.69.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.53.0>:class_Graph_NodeImpl,<0.54.0>:class_Graph_NodeImpl,750,\"<0.69.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.70.0>:class_Graph_EdgeImpl\"]Edge <0.70.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.54.0>:class_Graph_NodeImpl,<0.55.0>:class_Graph_NodeImpl,250,\"<0.70.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.71.0>:class_Graph_EdgeImpl\"]Edge <0.71.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.55.0>:class_Graph_NodeImpl,<0.56.0>:class_Graph_NodeImpl,100,\"<0.71.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.72.0>:class_Graph_EdgeImpl\"]Edge <0.72.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.56.0>:class_Graph_NodeImpl,<0.57.0>:class_Graph_NodeImpl,4000,\"<0.72.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.73.0>:class_Graph_EdgeImpl\"]Edge <0.73.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.57.0>:class_Graph_NodeImpl,<0.58.0>:class_Graph_NodeImpl,275,\"<0.73.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.74.0>:class_Graph_EdgeImpl\"]Edge <0.74.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.58.0>:class_Graph_NodeImpl,<0.59.0>:class_Graph_NodeImpl,750,\"<0.74.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.75.0>:class_Graph_EdgeImpl\"]Edge <0.75.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.59.0>:class_Graph_NodeImpl,<0.60.0>:class_Graph_NodeImpl,250,\"<0.75.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.76.0>:class_Graph_EdgeImpl\"]Edge <0.76.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.60.0>:class_Graph_NodeImpl,<0.61.0>:class_Graph_NodeImpl,100,\"<0.76.0>:class_Graph_EdgeImpl\");\n",
                "[HTTPName: \"<0.77.0>:class_Graph_EdgeImpl\"]Edge <0.77.0>:class_Graph_EdgeImpl = new local EdgeImpl(app,<0.61.0>:class_Graph_NodeImpl,<0.46.0>:class_Graph_NodeImpl,4000,\"<0.77.0>:class_Graph_EdgeImpl\");\n",
               };
         ArrayList<String> toFind = new ArrayList<>(Arrays.asList(toFindArray));
         int count = 0;

         Context context = new GraphParser().parse("src/test/resources/test5.zug.clean");
         for(Edge edge : Edge.in(context).getAll())
            {assertTrue(toFind.contains(edge.toABS()));
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
        {String toFindArray[] = {"[HTTPName: \"e01\"]Edge e01 = new local EdgeImpl(app,n01,n02,200,\"e01\");\n",
                            "[HTTPName: \"e02\"]Edge e02 = new local EdgeImpl(app,n02,n03,100,\"e02\");\n",
                            "[HTTPName: \"e03\"]Edge e03 = new local EdgeImpl(app,n03,n04,100,\"e03\");\n",
                            "[HTTPName: \"e04\"]Edge e04 = new local EdgeImpl(app,n04,n05,100,\"e04\");\n",
                            "[HTTPName: \"e05\"]Edge e05 = new local EdgeImpl(app,n05,n06,100,\"e05\");\n",
                            "[HTTPName: \"e06\"]Edge e06 = new local EdgeImpl(app,n06,n07,200,\"e06\");\n",
                            "[HTTPName: \"e07\"]Edge e07 = new local EdgeImpl(app,n07,n08,100,\"e07\");\n",
                            "[HTTPName: \"e08\"]Edge e08 = new local EdgeImpl(app,n08,n09,200,\"e08\");\n",
                            "[HTTPName: \"e09\"]Edge e09 = new local EdgeImpl(app,n09,n10,100,\"e09\");\n",
                            "[HTTPName: \"e10\"]Edge e10 = new local EdgeImpl(app,n10,n11,100,\"e10\");\n",
                            "[HTTPName: \"e11\"]Edge e11 = new local EdgeImpl(app,n10,n13,100,\"e11\");\n",
                            "[HTTPName: \"e12\"]Edge e12 = new local EdgeImpl(app,n11,n12,100,\"e12\");\n",
                            "[HTTPName: \"e13\"]Edge e13 = new local EdgeImpl(app,n13,n14,100,\"e13\");\n"};
         ArrayList<String> toFind = new ArrayList<>(Arrays.asList(toFindArray));
         int count = 0;

         Context context = new GraphParser().parse("src/test/resources/test9.zug.clean");
         for(Edge edge : Edge.in(context).getAll())
            {assertTrue(toFind.contains(edge.toABS()));
             count++;
            }
         assertTrue(count == toFindArray.length);
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
