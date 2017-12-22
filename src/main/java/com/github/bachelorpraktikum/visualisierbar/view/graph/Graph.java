package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.*;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.graph.elements.Elements;

import java.util.*;

import javafx.scene.Group;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Graph {

    @Nonnull
    private final Context context;
    @Nonnull
    private final CoordinatesAdapter coordinatesAdapter;

    @Nonnull
    private final Group group;

    @Nonnull
    private  Map<Node, GraphShape<Node>> nodes;
    @Nonnull
    private  Map<Edge, GraphShape<Edge>> edges;
    @Nonnull
    private Map<Element, GraphShape<Element>> elements;


    /**
     * Creates a new graph for the given context. The graph is laid out by using the given {@link
     * CoordinatesAdapter}.
     *
     * @param context the context
     * @param coordinatesAdapter the coordinates adapter to translate coordinates from the model to
     * real coordinates
     * @throws NullPointerException if either argument is null
     * @throws IllegalStateException if there is nothing for this context to show
     */
    public Graph(Context context, CoordinatesAdapter coordinatesAdapter) {
        this.context = Objects.requireNonNull(context);
        this.coordinatesAdapter = Objects.requireNonNull(coordinatesAdapter);
        this.nodes = new LinkedHashMap<>(128);
        this.elements = new LinkedHashMap<>(256);
        this.group = new Group();
        this.edges = new LinkedHashMap<>(256);
        for (Edge edge : Edge.in(context).getAll()) {
            GraphShape<Edge> shape = new Rail(edge, coordinatesAdapter);
            edges.put(edge, shape);
            group.getChildren().add(shape.getFullNode());
        }

        for (Node node : Node.in(context).getAll()) {
            GraphShape<Node> shape = new Junction(node, coordinatesAdapter);

            nodes.put(node, shape);
            group.getChildren().add(shape.getFullNode());

            for (GraphShape<Element> elementShape : Elements.create(node, coordinatesAdapter)) {
                for (Element element : elementShape.getRepresentedObjects()) {
                    elements.put(element, elementShape);
                }
                group.getChildren().add(elementShape.getFullNode());
            }
        }
    }

    public void scale(double factor) {
        double scale = group.getScaleX() * factor;
        group.setScaleX(scale);
        group.setScaleY(scale);
    }

    public void move(double x, double y) {
        group.setTranslateX(group.getTranslateX() + x);
        group.setTranslateY(group.getTranslateY() + y);
    }

    public Group getGroup() {
        return group;
    }

    public Map<Node, GraphShape<Node>> getNodes() {
        return nodes;
    }

    public Map<Edge, GraphShape<Edge>> getEdges() {
        return edges;
    }

    public Map<Element, GraphShape<Element>> getElements() {
        return elements;
    }

    public CoordinatesAdapter getCoordinatesAdapter() {
        return coordinatesAdapter;
    }

    public void removeNode(Node node){
        LinkedList<Element> e =new LinkedList<>();
        //LinkedList<Node> n =new LinkedList();
        LinkedList<Edge> ed =new LinkedList<>();
        elements.forEach((a,b)->{
            if (a.getNode()==node){
                e.add(a);
                group.getChildren().remove(b.getFullNode());
                //elements.remove(a);
            }
        });

        edges.forEach((a,b)->{
            if(a.getNode1()==node || a.getNode2()==node) {
                ed.add(a);
                group.getChildren().remove(b.getFullNode());
                //edges.remove(a);
            }
        });
        e.forEach((a)->{
            elements.remove(a);
            Element.in(context).remove(a);
            context.removeObject(a);
        });
        ed.forEach((a)->{
            Edge.in(context).remove(a);
            edges.remove(a);
            context.removeObject(a);
        });
        group.getChildren().remove(nodes.get(node).getFullNode());
        nodes.remove(node);
        Node.in(context).remove(node);
        context.removeObject(node);



    }
    public void removeEdge (Edge edge){

        edges.remove(edge);
        context.removeObject(edge);
    }

    public void fullyConnect (HashSet<Junction> selection){
        //Edge edge = Edge.in(context).create()

    }

    public void disconnect (Node node){
        LinkedList<Edge> ed = new LinkedList<>();
        edges.forEach((a,b)-> {
            if (a.getNode1() == node || a.getNode2() == node) {
                ed.add(a);
                group.getChildren().remove(b.getFullNode());
            }
        });
        ed.forEach((a)->{
            edges.remove(ed);
            context.removeObject(ed);
        });
    }

    public void addNode (String name, Coordinates coordinates) throws IllegalArgumentException {
        Node newNode =Node.in(context).create(name, coordinates);
        if(nodes.containsKey(newNode)) return;
        context.addObject(newNode);
        GraphShape<Node> shape = new Junction(newNode, coordinatesAdapter);
        ((Junction) shape).setMoveable(true);

        nodes.put(newNode, shape);
        group.getChildren().add(shape.getFullNode());

    }
}
