package com.github.bachelorpraktikum.visualisierbar.view.graph;

import com.github.bachelorpraktikum.visualisierbar.model.*;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.CoordinatesAdapter;
import com.github.bachelorpraktikum.visualisierbar.view.graph.elements.Elements;


import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import com.sun.istack.internal.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class Graph {

    @Nonnull
    private SimpleBooleanProperty change;
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
        this.change = new SimpleBooleanProperty(false);
        this.context = Objects.requireNonNull(context);
        this.coordinatesAdapter = Objects.requireNonNull(coordinatesAdapter);
        this.nodes = new LinkedHashMap<>(128);
        this.elements = new LinkedHashMap<>(256);
        this.group = new Group();
        this.edges = new LinkedHashMap<>(256);

        Junction.clearSelection();
        for (Node node : Node.in(context).getAll()) {
            node.setGraph(null);                                                                                          //in case this is a graph switch we need to null this so everything gets
            node.getEdges().forEach(a-> a.setGraph(null));                                                                //properly redrawn
            node.getElements().forEach(a-> a.setGraph(null));
        }
        for (Node node : Node.in(context).getAll()) {
            enterNode(node);
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

    /**
     * removes a {@link Node} from the Graph and the Factory mapping.
     * removes any {@link Edge} from the Graph, Factory and connected Nodes that contain this node
     * removes any {@link Element} from the Graph and Factory that is attached to this node (i.e WeichenpunktElement is removed if one of the Elements is attached to this node)
     * Also nulls direction of Elements pointing to this node and removes Elements from their {@link LogicalGroup}
     * This messes with the Node, so dont use it in any other way after calling this function.
     * @param node the Node to remove
     * @throws NullPointerException if node is null
     */
    public void removeNode(@Nonnull Node node){
        Objects.requireNonNull(node);
        LinkedList<Element> e =new LinkedList<>();
        LinkedList<Edge> ed =new LinkedList<>();
        elements.forEach((a,b)->{
            if (a.getNode()==node){
                e.add(a);
            }
        });

        e.forEach(this::removeElement);

        ed.addAll(node.getEdges());
        ed.forEach(this::removeEdge);

        group.getChildren().remove(nodes.get(node).getFullNode());
        node.setGraph(null);
        nodes.remove(node);                                                                                                 //remove node from graph, factory context and graph pane
        Node.in(context).remove(node);
        changed();

    }

    /**
     * removes the edge from both nodes, the Graph, the context and factory.
     * Also nulls direction if it becomes invalid due to edge removal
     * Also nulls direction of Elements that become invalid due to the missing edge
     * @param edge the edge to remove
     */
    public void removeEdge (@Nonnull Edge edge){
        Objects.requireNonNull(edge);
        Node node1= edge.getNode1();
        Node node2= edge.getNode2();
        for(Element element : node1.getElements()){
            if (element.getDirection()== node2)
                element.setDirection(null);
        }
        for(Element element : node2.getElements()){
            if (element.getDirection()== node1)
                element.setDirection(null);
        }
        group.getChildren().remove(edges.get(edge).getFullNode());
        edges.remove(edge);
        node1.getEdges().remove(edge);
        node2.getEdges().remove(edge);
        Edge.in(context).remove(edge);
        edge.setGraph(null);
        changed();
    }



    /**
     * Connects every Node in the Selection with every other Node part of the Selection.
     * Already existing Edges are not duplicated.
     * New Edges are given a random name and length -1
     * New Edges are added to the Graph and the Factory mapping.
     * @param NodeSet the Selection to fully connect
     */
    public void fullyConnect (Set<Node> NodeSet){


        LinkedList<Node> sList = new LinkedList<>();                                                                        //turn into list to get an order
        sList.addAll(NodeSet);
        int l = NodeSet.size();
        boolean existingEdges[][]=new boolean [l][l];                                                                       //mark existing edges in this array

        for (int i=0; i<l; i++){
            for (int k=0; k<l ; k++ ){
                //init array
                existingEdges[i][k] = i == k;
            }
        }
        edges.forEach((a,b)->{
            if (NodeSet.contains(a.getNode1()) && NodeSet.contains(a.getNode2())) {
                int i;
                int j;
                for ( i = 0; i < l; i++) {
                    if(a.getNode1()== sList.get(i)) break;
                }
                for ( j = 0; j < l; j++) {
                    if(a.getNode2()== sList.get(j)) break;
                }
                existingEdges[i][j]=true;                                                                                   //mark the existing edge
                existingEdges[j][i]=true;
            }
        });

        //RandomString gen = new RandomString(8, ThreadLocalRandom.current());
        for (int i=0; i<l; i++){
            for (int k=i; k<l ; k++ ){
                if (!existingEdges[i][k]){
                    String name="e0";

                    /*for (int j=0; j<1000; j++) {                                                                            //generate missing edges with random names
                        name = gen.nextString();
                        if(!Edge.in(context).NameExists(name)) break;
                    }*/

                    for(int j=0; Edge.in(context).NameExists(name) || Edge.in(context).AbsNameExists(name, null) ; j++)
                        {name = "e".concat(String.valueOf(j));}
                    Edge edge = Edge.in(context).create(name,-1, sList.get(i), sList.get(k));
                    edge.setGraph(this);
                    GraphShape<Edge> shape = new Rail(edge, coordinatesAdapter);
                    edges.put(edge, shape);
                    group.getChildren().add(shape.getFullNode());
                }
            }
        }
        changed();

    }

    /**
     * Removes all Edges that are associated with this Node.
     * Also nulls direction if it becomes invalid because of the Edge removal
     * Edges are removed from the Graph and the Factory mapping.
     * @param node to disconnect
     * @throws NullPointerException if node is null
     */
    public void disconnect (@Nonnull Node node){
        LinkedList<Edge> ed = new LinkedList<>();

        ed.addAll(node.getEdges());
        ed.forEach(this::removeEdge);
        changed();
    }

    /**
     * Removes an element from the Graph
     * @param element Element to remove
     */
    public void removeElement (@Nonnull Element element){
        Objects.requireNonNull(element);

        if(element.getType()== Element.Type.WeichenPunkt){
            if (element== element.getSwitch().getMainElement()){
                for(Element we : element.getSwitch().getElements()){
                rE(we);
                }
            }
            else
                removeElement(element.getSwitch().getMainElement());
        }
        else
            rE(element);

        changed();
    }

    private void rE(Element element){
        if(element.getLogicalGroup()!= null){
            element.getLogicalGroup().removeElement(element);           //remove from group
        }
        element.getNode().getElements().remove(element);
        elements.get(element).getRepresentedObjects().remove(element);
        group.getChildren().remove(elements.get(element).getFullNode());        //TODO only remove when no other represented Objects?
        elements.remove(element);
        Element.in(context).remove(element);                      //remove elements from context, factory and graph
        element.setGraph(null);
        if(element.getType().isComposite()){
            rebuildComposite(element.getNode());
        }

    }

    /**
     * rebuilds the Composite Elements of a Node. Call this after modifying a composite Element
     * @param node to rebuild
     */
    public void rebuildComposite (@Nonnull Node node){
        Objects.requireNonNull(node);
        for( Element CompositeElement : node.getElements()){
            if (CompositeElement.getType().isComposite()){                                                                  //remove composite Elements to rebuild them
                if(CompositeElement.getGraph()==this){
                    this.getGroup().getChildren().remove(elements.get(CompositeElement).getFullNode());
                    CompositeElement.setGraph(null);
                }
            }
        }
        for (GraphShape<Element> elementShape : Elements.create(node, coordinatesAdapter)) {
            for (Element Celement : elementShape.getRepresentedObjects()) {
            elements.put(Celement, elementShape);
            Celement.setGraph(this);
            }
            group.getChildren().add(elementShape.getFullNode());
        }
        changed();
    }

    /**
     * Enter a node, its edges and elements into the graph
     * @param node the node to enter
     */

    public void enterNode (@Nonnull Node node){
        Objects.requireNonNull(node);
        for (Edge edge : node.getEdges()){
            if(edge.getGraph()==null){
                GraphShape<Edge> EdgeShape = new Rail(edge, coordinatesAdapter);
                edges.put(edge, EdgeShape);
                edge.setGraph(this);
                group.getChildren().add(EdgeShape.getFullNode());

            }
        }

        GraphShape<Node> shape = new Junction(node, coordinatesAdapter);

        nodes.put(node, shape);
        node.setGraph(this);
        group.getChildren().add(shape.getFullNode());


        for (GraphShape<Element> elementShape : Elements.create(node, coordinatesAdapter)) {

            for (Element element : elementShape.getRepresentedObjects()) {
                elements.put(element, elementShape);
                element.setGraph(this);
            }
            group.getChildren().add(elementShape.getFullNode());
        }

        changed();
    }



    /**
     * Adds a new Node with the specified name and {@link Coordinates} to the Graph and the Factory mapping
     * @param name the Name
     * @param coordinates   the coordinates
     * @throws IllegalArgumentException    if coordinates are negative or name is taken
     * @throws NullPointerException if either argument is null
     */
    public void addNode (@Nonnull String name, @Nonnull Coordinates coordinates) throws IllegalArgumentException {
        Node newNode =Node.in(context).create(Objects.requireNonNull(name), Objects.requireNonNull(coordinates));
        newNode.setGraph(this);
        if(nodes.containsKey(newNode)) return;
        GraphShape<Node> shape = new Junction(newNode, coordinatesAdapter);
        ((Junction) shape).setMoveable(true);

        nodes.put(newNode, shape);
        group.getChildren().add(shape.getFullNode());
        changed();
    }


    /**
     * Adds a new edge with the specified name and attributes to the Graph and the Factory mapping
     * @param name name of the new edge
     * @param node1 starting Node of the edge
     * @param node2 ending Node of the edge
     * @param length length of the edge
     * @param absName Abs-Name of the edge (optional, if not used set it to null)
     */
    public void addEdge(@Nonnull String name, @Nonnull Node node1, @Nonnull Node node2, int length, @Nullable String absName)
        {Edge newEdge = Edge.in(context).create(name, length, node1, node2, absName);
         newEdge.setGraph(this);
         if(edges.containsKey(newEdge)) return;
         GraphShape<Edge> shape = new Rail(newEdge, coordinatesAdapter);
         edges.put(newEdge, shape);
         group.getChildren().add(shape.getFullNode());

         changed();
        }


    /**
     * Adds a new Node with the specified name and {@link Coordinates} to the Graph and the Factory mapping
     * @param name the Name
     * @param coordinates   the coordinates
     * @param absName the ABS Name
     * @throws IllegalArgumentException    if coordinates are negative or name is taken
     * @throws NullPointerException if any argument is null
     */
    public void addNode (@Nonnull String name, @Nonnull Coordinates coordinates, @Nonnull String absName) throws IllegalArgumentException {
        Node newNode =Node.in(context).create(Objects.requireNonNull(name), Objects.requireNonNull(coordinates), Objects.requireNonNull(absName));
        newNode.setGraph(this);
        if(nodes.containsKey(newNode)) return;
        GraphShape<Node> shape = new Junction(newNode, coordinatesAdapter);
        ((Junction) shape).setMoveable(true);

        nodes.put(newNode, shape);
        group.getChildren().add(shape.getFullNode());
        changed();
    }


    /**
     * Adds an Element to the Graph
     * @param elementToAdd element to add
     */
    public void addElement(@Nonnull Element elementToAdd){
        Objects.requireNonNull(elementToAdd);

        if(elementToAdd.getType().isComposite()){
            for( Element element : elementToAdd.getNode().getElements()){
                if (element.getType().isComposite()){                       //remove composite Elements to rebuild them with the new element if necessary
                    if(element.getGraph()==this){
                        this.getGroup().getChildren().remove(elements.get(element).getFullNode());
                        element.setGraph(null);
                    }
                }
            }
        }

        for (GraphShape<Element> elementShape : Elements.create(elementToAdd.getNode(), coordinatesAdapter)) {
            for (Element element : elementShape.getRepresentedObjects()) {
                elements.put(element, elementShape);
                element.setGraph(this);
            }
            group.getChildren().add(elementShape.getFullNode());

        }
        changed();
    }

    @Nonnull
    public Context getContext(){
        return this.context;
    }

    /**
     * Generating the Nodes ABS-Code
     * @param prefix the prefix of each line (e.g. \t)
     * @return a String of all Nodes in ABS-Code-Format
     */
    @Nonnull
    public String printNodesToAbs(String prefix)
        {String response = prefix;
         for(Map.Entry<Node, GraphShape<Node>> entry : nodes.entrySet())
            {response = response.concat(entry.getKey().toABS());}
         response = response.replace("\n", "\n" + prefix);
         return response;
        }

    /**
     * Generating the edges ABS-Code
     * @param prefix the prefix of each line (e.g. \t)
     * @return a String of all edges in ABS-Code-Format
     */
    @Nonnull
    public String printEdgesToAbs(String prefix)
        {String response = prefix;
         for(Map.Entry<Edge, GraphShape<Edge>> entry : edges.entrySet())
            {response = response.concat(entry.getKey().toABS());}
         response = response.replace("\n", "\n" + prefix);
         return response;
        }

    /**
     * Generating the elements ABS-Code
     * @param prefix the prefix of each line (e.g. \t)
     * @return a String of all elements in ABS-Code-Format
     */
    @Nonnull
    public String printElementsToAbs(@Nonnull String prefix, @Nullable String deltaContent)
        {String response = prefix;
         for(Map.Entry<Element, GraphShape<Element>> entry : elements.entrySet())
            {response = response.concat(entry.getKey().toABS(deltaContent));}
         response = response.replace("\n", "\n" + prefix);
         return response;
        }

    /**
     * Generating the logicalGroups ABS-Code
     * @param prefix the prefix of each line (e.g. \t)
     * @return a String of all logicalGroups in ABS-Code-Format
     */
    @Nonnull
    public String printLogicalGroupsToAbs(@Nonnull String prefix, @Nullable String deltaContent)
        {String response = prefix;
            for (LogicalGroup iteration_element : LogicalGroup.in(context).getAll()) {
                response = response.concat(iteration_element.toABS(deltaContent));
            }
         response = response.replace("\n", "\n" + prefix);
         return response;
        }

    /**
     * Printing the Graph to ABS-Code to the console
     * @return a String of all Nodes, Elements, Edges and LogicalGroups in ABS-Code-Format
     */
    @Nonnull
    public String printToAbs() {
        String response = "";
        response = response.concat(printNodesToAbs(""));
        response = response.concat("\n");

        response = response.concat(printEdgesToAbs(""));
        response = response.concat("\n");

        response = response.concat(printElementsToAbs("", null));
        response = response.concat("\n");

        response = response.concat(printLogicalGroupsToAbs("", null));

        // Print it! Remove if you don't need this!
        System.out.println("----- ABS start -----");
        System.out.println(response);
        System.out.println("----- ABS end -----");
        // Stop removing here!

        return response;
    }

    public void changed(){
        change.setValue(!change.getValue());
    }

    @Nonnull
    public SimpleBooleanProperty changeProperty() {
        return change;
    }
}
