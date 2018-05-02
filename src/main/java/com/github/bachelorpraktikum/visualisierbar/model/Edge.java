package com.github.bachelorpraktikum.visualisierbar.model;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.Line;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

/**
 * Represents an edge between 2 {@link Node nodes}.<br>
 * Edges are immutable. There is only one instance of Edge per name per {@link Context}.
 */
@Immutable
@ParametersAreNonnullByDefault
public final class Edge implements GraphObject<Line> {

    private static final Logger log = Logger.getLogger(Edge.class.getName());

    @Nonnull
    private  String name;
    @Nullable
    private String absName;

    private  int length;
    @Nonnull
    private final Node node1;
    @Nonnull
    private final Node node2;
    private final Property<VisibleState> stateProperty;
    @Nullable
    private Graph graph;

   private Edge(String name, int length, Node node1, Node node2) {
        this.name = Objects.requireNonNull(name);
        this.length = length;
        this.graph=null;
        this.node1 = Objects.requireNonNull(node1);
        this.node2 = Objects.requireNonNull(node2);
        node1.addEdge(this);
        node2.addEdge(this);
        //this.graph=graph;

        this.stateProperty = new SimpleObjectProperty<>();
    }

    private Edge(String name, int length, Node node1, Node node2,@Nullable String absName) {
        this.absName = absName;
        this.name = Objects.requireNonNull(name);
        this.length = length;
        this.graph=null;
        this.node1 = Objects.requireNonNull(node1);
        this.node2 = Objects.requireNonNull(node2);
        node1.addEdge(this);
        node2.addEdge(this);
        //this.graph=graph;

        this.stateProperty = new SimpleObjectProperty<>();
    }

    /**
     * Manages all instances of {@link Edge}.
     */
    @ParametersAreNonnullByDefault
    public static final class EdgeFactory implements Factory<Edge> {

        private static final int INITIAL_EDGES_CAPACITY = 512;
        private static final Map<Context, WeakReference<EdgeFactory>> instances = new WeakHashMap<>();

        @Nonnull
        private final Map<String, Edge> edges;
        @Nonnull
        private final Factory<Node> nodeFactory;

        @Nonnull
        private static EdgeFactory getInstance(Context context) {

            EdgeFactory result = instances.computeIfAbsent(context, ctx -> {
                EdgeFactory factory = new EdgeFactory(ctx);
                ctx.addObject(factory);
                return new WeakReference<>(factory);
            }).get();

            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        }

        private EdgeFactory(Context ctx) {
            this.edges = new LinkedHashMap<>(INITIAL_EDGES_CAPACITY);
            this.nodeFactory = Node.in(ctx);
        }

        /**
         * Potentially creates a new instance of {@link Edge}.
         *
         * @param name the unique name of the edge
         * @param length the length in meters
         * @param node1 the first declared node at the end of the edge
         * @param node2 the second declared node at the end of the edge
         * @return an instance of Edge
         * @throws NullPointerException if at least one of the arguments is null
         * @throws IllegalArgumentException if an edge with the same name but different parameters
         * already exists
         * @throws IllegalArgumentException if either of the given nodes are not from within the
         * same context
         */
        @Nonnull
        public Edge create(String name, int length, Node node1, Node node2) {
            if (!nodeFactory.checkAffiliated(node1) || !nodeFactory.checkAffiliated(node2)) {
                throw new IllegalArgumentException("at least one node is from the wrong context");
            }

            Edge result = edges.computeIfAbsent(Objects.requireNonNull(name), edgeName ->
                    new Edge(edgeName, length, node1, node2)
            );

            if (result.getLength() != length
                    || !result.getNode1().equals(node1)
                    || !result.getNode2().equals(node2)) {
                String edgeFormat = "(length: %d, node1: %s, node2: %s)";
                String message = "Edge with name: %s already exists:\n"
                        + edgeFormat + ", tried to recreate with following arguments:\n"
                        + edgeFormat;
                message = String.format(message, name, length, node1, node2,
                        result.getLength(), result.getNode1(), result.getNode2());
                throw new IllegalArgumentException(message);
            }

            return result;
        }

        /**
         * Potentially creates a new instance of {@link Edge}.
         *
         * @param name the unique name of the edge
         * @param length the length in meters
         * @param node1 the first declared node at the end of the edge
         * @param node2 the second declared node at the end of the edge
         * @param absName the ABS name of the edge
         * @return an instance of Edge
         * @throws NullPointerException if at least one of the arguments is null
         * @throws IllegalArgumentException if an edge with the same name but different parameters
         * already exists
         * @throws IllegalArgumentException if either of the given nodes are not from within the
         * same context
         */
        @Nonnull
        public Edge create(String name, int length, Node node1, Node node2, @Nullable String absName) {
            if (!nodeFactory.checkAffiliated(node1) || !nodeFactory.checkAffiliated(node2)) {
                throw new IllegalArgumentException("at least one node is from the wrong context");
            }

            Edge result = edges.computeIfAbsent(Objects.requireNonNull(name), edgeName ->
                new Edge(edgeName, length, node1, node2, absName)
            );

            if (result.getLength() != length
                || !result.getNode1().equals(node1)
                || !result.getNode2().equals(node2)) {
                String edgeFormat = "(length: %d, node1: %s, node2: %s)";
                String message = "Edge with name: %s already exists:\n"
                    + edgeFormat + ", tried to recreate with following arguments:\n"
                    + edgeFormat;
                message = String.format(message, name, length, node1, node2,
                    result.getLength(), result.getNode1(), result.getNode2());
                throw new IllegalArgumentException(message);
            }

            return result;
        }

        @Override
        @Nonnull
        public Edge get(String name) {
            Edge edge = edges.get(Objects.requireNonNull(name));
            if (edge == null) {
                throw new IllegalArgumentException("unknown edge: " + name);
            }
            return edge;
        }

        /**
         * Checks the availability of a name
         * @param name the String to check
         * @return true if an Edge with this name exists, otherwise false
         */
        public boolean NameExists (@Nonnull String name){
            Edge edge = edges.get(Objects.requireNonNull(name));
            return edge != null;
        }

        /**
         * Checks the availability of a ABS name
         * @param name the String to check
         * @return true, if an Edge with this name exists, otherwise false
         */
        Boolean AbsNameExists(@Nonnull String name, @Nullable Edge edge)
            {for(Map.Entry<String, Edge> entry : edges.entrySet())
                {if(Objects.equals(entry.getValue().getAbsName(), name) && !entry.getValue().equals(edge))
                    {return true;}
                }
             return false;
            }

        public void remove(Edge edge){
            edges.remove(edge.getName());
        }

        @Override
        @Nonnull
        public Collection<Edge> getAll() {
            return Collections.unmodifiableCollection(edges.values());
        }

        @Override
        public boolean checkAffiliated(@Nonnull Edge edge) {
            return edges.get(edge.getName()) == edge;
        }
    }

    /**
     * Gets the {@link EdgeFactory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if the context is null
     */
    @Nonnull
    public static EdgeFactory in(Context context) {
        return EdgeFactory.getInstance(context);
    }

    /**
     * Gets the length of this {@link Edge}.
     *
     * @return the length in meters
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the node declared first.
     *
     * @return the first node
     */
    @Nonnull
    public Node getNode1() {
        return node1;
    }

    /**
     * Gets the node declared second.
     *
     * @return the second node
     */
    @Nonnull
    public Node getNode2() {
        return node2;
    }

    /**
     * Gets the node on the other end of this edge.
     *
     * @param node the node on this edge you're not looking for
     * @return the node on this edge not passed as an argument
     * @throws NullPointerException if node is null
     * @throws IllegalArgumentException if the specified node is not on this edge
     */
    @Nonnull
    public Node getOtherNode(Node node) {
        Objects.requireNonNull(node);
        if (getNode1().equals(node)) {
            return getNode2();
        } else if (getNode2().equals(node)) {
            return getNode1();
        } else {
            throw new IllegalArgumentException(String.format(
                "Node %s is not on Edge %s",
                node.getName(),
                this
            ));
        }
    }

    /**
     * Gets the Node this Edge and the given Edge have in common.
     *
     * @param other the other Edge
     * @return the common Node
     * @throws NullPointerException if other is null
     * @throws IllegalArgumentException if this edge doesn't have an Edge in common with the given
     * one
     */
    @Nonnull
    public Node getCommonNode(Edge other) {
        Objects.requireNonNull(other);
        Node n1 = getNode1();
        Node n2 = getNode2();

        if (n1.equals(other.getNode1()) || n1.equals(other.getNode2())) {
            return n1;
        } else if (n2.equals(other.getNode1()) || n2.equals(other.getNode2())) {
            return n2;
        } else {
            throw new IllegalArgumentException();
        }
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Nullable
    public String getAbsName() {return absName;}

    /**
     * set the ABS name of this edge
     * @param newAbsName the new ABS name
     * @return true if the changes was successfull, false if name already taken or null
     */
    boolean setAbsName(@Nullable String newAbsName)
        {if(newAbsName == null) {return false;}
         if(graph != null) {
             boolean exit = Edge.in(graph.getContext()).AbsNameExists(newAbsName, this);
             if (!exit) {
                 this.absName = newAbsName;
                 Edge.in(graph.getContext()).edges.remove(name);
                 Edge.in(graph.getContext()).edges.put(name, this);
                 return true;
             }
         }
         return false;
        }

    /**
     * set the name of this Edge
     * @param newName the name
     * @return true if change was succesfull, false if name already taken
     */
    public boolean setName(@Nonnull String newName){
        if (graph!= null){

            if(!Edge.in(graph.getContext()).NameExists(newName)){
                this.name=newName;
                Edge.in(graph.getContext()).edges.remove(newName);
                Edge.in(graph.getContext()).edges.put(newName,this);
                return true;
            }
        }
        return false;

    }

    /**
     * Sets the length of this Edge.
     *
     * @param length a positive Integer
     * @return true if the change was successful, false otherwise
     */

    public boolean setLength(int length){
        if (length<0){return false;}
        else{
            this.length=length;
        }
        return true;
    }

    @Nullable
    @Override
    public Graph getGraph() {
        return this.graph;
    }

    public void setGraph(@Nullable Graph graph){
        this.graph=graph;
    }

    @Nonnull
    @Override
    public Line createShape() {
        return new Line();
    }

    @Nonnull
    @Override
    public Property<VisibleState> visibleStateProperty() {
        return stateProperty;
    }

    @Override
    public String toString() {
        return "Edge{"
            + "name='" + name + '\''
            + ", length=" + length
            + ", node1=" + node1
            + ", node2=" + node2
            + '}';
    }


    /**
     * Returns a String of the ABS representation of this Node.
     * If there is no ABS-Name compiled, it will use the Erlang-Name
     *
     * @return the ABS-Code
     */
    @Nonnull
    public String toABS(){
        String nameOfEdge = higherName();
        String nameOfNode1 = getNode1().higherName();
        String nameOfNode2 = getNode2().higherName();

        return String.format("[HTTPName: \"%s\"]Edge %s = new local EdgeImpl(%s,%s,%s,%s,\"%s\");\n",
                nameOfEdge, nameOfEdge, "app", nameOfNode1, nameOfNode2, length, nameOfEdge);
    }

    /**
     * Returns the highest rated, available name of this Edge.
     * Rating: ABSName > ErlangName
     *
     * @return the higher rated, available name
     */
    @Nonnull
    public String higherName()
        {return absName == null ? name : absName;}
}
