package com.github.bachelorpraktikum.visualisierbar.model;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.Circle;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a node on the track.<br>
 * There is only one instance of Node per name per {@link Context}.
 */
@ParametersAreNonnullByDefault
public final class Node implements GraphObject<Circle> {

    private static final Logger log = Logger.getLogger(Node.class.getName());

    @Nonnull
    private  String name;
    @Nonnull
    private  Coordinates coordinates;
    @Nonnull
    private  Set<Edge> edges;
    @Nonnull
    private  Set<Element> elements;
    @Nonnull
    private final Property<VisibleState> stateProperty;
    //indicates change in the coordinates
    @Nonnull
    private final BooleanProperty movedProperty;

    private final Context context;

    private Node(String name, Coordinates coordinates, Context context) {
        this.name = Objects.requireNonNull(name);
        this.coordinates = Objects.requireNonNull(coordinates);
        this.edges = new LinkedHashSet<>();
        this.elements = new HashSet<>();
        this.stateProperty = new SimpleObjectProperty<>();
        this.movedProperty = new SimpleBooleanProperty(false);
        this.context = context;
    }

    @Nonnull
    @Override
    public Circle createShape() {
        return new Circle(1);
    }

    /**
     * Manages all instances of {@link Node}.<br>
     * Ensures there is always only one instance of node per name per {@link Context}.
     */
    public static final class NodeFactory implements Factory<Node> {

        private static final int INITIAL_NODES_CAPACITY = 128;
        private static final Map<Context, WeakReference<NodeFactory>> instances = new WeakHashMap<>();

        @Nonnull
        private final Map<String, Node> nodes;
        private final Context context;

        @Nonnull
        private static NodeFactory getInstance(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            NodeFactory result = instances.computeIfAbsent(context, ctx -> {
                NodeFactory factory = new NodeFactory(ctx);
                ctx.addObject(factory);
                return new WeakReference<>(factory);
            }).get();

            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        }

        private NodeFactory(Context ctx) {
            this.context=ctx;
            this.nodes = new LinkedHashMap<>(INITIAL_NODES_CAPACITY);
        }

        /**
         * Potentially creates a new instance of {@link Node}.<br>
         * If an node with the same name already exists, it is returned.
         *
         * @param name the unique name of this node
         * @param coordinates the {@link Coordinates} of this node
         * @return an element
         * @throws NullPointerException if either of the arguments is null
         * @throws IllegalArgumentException if a node with the same name but different coordinates
         * already exists
         */
        @Nonnull
        public Node create(String name, Coordinates coordinates) {
            Node result = nodes.computeIfAbsent(Objects.requireNonNull(name), nodeName ->
                new Node(nodeName, coordinates, context)
            );

            if (!result.getCoordinates().equals(coordinates)) {
                String nodeFormat = "(Coordinates: %s)";
                String message = "Node with name: %s already exists:\n"
                    + nodeFormat + ", tried to recreate with following arguments:\n"
                    + nodeFormat;
                message = String.format(message, name, coordinates, result.getCoordinates());
                throw new IllegalArgumentException(message);
            }

            return result;
        }

        public boolean NameExists (String name){
            Node node = nodes.get(Objects.requireNonNull(name));
            if (node == null) {
                return false;
            }
            return true;
        }

        @Override
        @Nonnull
        public Node get(String name) {
            Node node = nodes.get(Objects.requireNonNull(name));
            if (node == null) {
                throw new IllegalArgumentException("unknown node: " + name);
            }
            return node;
        }

        public void remove(Node node){
            nodes.remove(node.getName());
        }

        @Override
        @Nonnull
        public Collection<Node> getAll() {
            return Collections.unmodifiableCollection(nodes.values());
        }

        @Override
        public boolean checkAffiliated(@Nonnull Node node) {
            return nodes.get(node.getName()) == node;
        }
    }

    /**
     * Gets the {@link NodeFactory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if context is null
     */
    public static NodeFactory in(Context context) {
        return NodeFactory.getInstance(context);
    }

    void addEdge(Edge edge) {
        edges.add(Objects.requireNonNull(edge));
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public Context getContext() {
       return this.context;
    }

    /**
     * changes the name of a node
     * @param newName the new Name the node will have
     * @return true when the change was succesfull, false if it was not
     */
    public boolean setName(String newName){
        if(!Node.in(context).NameExists(newName)){
            this.name=newName;
            Node.in(this.getContext()).nodes.remove(newName);
            Node.in(this.getContext()).nodes.put(newName,this);
            return true;
        }
      return false;
    }


    /**
     * Gets the coordinates of this node.
     *
     * @return the coordinates
     */
    @Nonnull
    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates a ){this.coordinates=a;}

    /**
     * Gets a set of all {@link Edge edges} connected with this node, except the given one.
     *
     * @param edge the edge to exclude from the set.
     * @return the set of edges
     * @throws NullPointerException if edge is null
     */
    @Nonnull
    public Set<Edge> otherEdges(Edge edge) {
        if (edge == null) {
            throw new NullPointerException("edge is null");
        }
        return edges.stream()
            .filter(e -> !e.equals(edge))
            .collect(Collectors.toSet());
    }

    /**
     * Gets all edges connected to this node.
     *
     * @return the edges
     */
    @Nonnull
    public Set<Edge> getEdges() {
        return Collections.unmodifiableSet(edges);
    }

    /**
     * Gets all elements at this node.
     *
     * @return the elements
     */
    @Nonnull
    public Set<Element> getElements() {
        return Collections.unmodifiableSet(elements);
    }

    void addElement(Element element) {
        elements.add(Objects.requireNonNull(element));
    }

    @Nonnull
    @Override
    public Property<VisibleState> visibleStateProperty() {
        return stateProperty;
    }

    public BooleanProperty movedProperty(){return movedProperty;}

    public void moved(){
        movedProperty.setValue(!movedProperty.getValue());
    }

    @Override
    public String toString() {
        return "Node{"
            + "name='" + name + '\''
            + ", coordinates=" + coordinates
            + '}';
    }
}
