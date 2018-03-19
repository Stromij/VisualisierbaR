package com.github.bachelorpraktikum.visualisierbar.model;
import com.github.bachelorpraktikum.visualisierbar.view.*;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import com.github.bachelorpraktikum.visualisierbar.view.graph.GraphShape;
import com.github.bachelorpraktikum.visualisierbar.view.graph.Junction;
import com.github.bachelorpraktikum.visualisierbar.view.graph.adapter.SimpleCoordinatesAdapter;
import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class GraphTest   {

    private Graph graph;
    //private Random cordGen = new Random();
    //private RandomString nameGen = new RandomString(2, ThreadLocalRandom.current());




    @Before
    public void init(){
        graph= new Graph(new Context(), new SimpleCoordinatesAdapter());
        com.sun.javafx.application.PlatformImpl.startup(()->{});
    }
    @After
    public void clean(){
        graph= new Graph(new Context(), new SimpleCoordinatesAdapter());
    }

    @AfterClass
    public static void end(){
        com.sun.javafx.application.PlatformImpl.exit();
    }

    @Test
    public void addNodeTest(){          //add five nodes to the graph
        int duplicateName = 0;
        int n = 5;
        for (int i=0; i<n; i++){
            String name= String.valueOf(i%4);
            if(Node.in(graph.getContext()).NameExists(name)) duplicateName++;
            try{graph.addNode(name, new Coordinates(i, i));}
            catch (IllegalArgumentException e) {assertTrue (duplicateName==1);}
            assertTrue(graph.getNodes().containsKey(Node.in(graph.getContext()).get(name)));
            assertTrue (Node.in(graph.getContext()).NameExists(name));                  //Node in Factory
        }
        assertTrue (graph.getNodes().size()== n-duplicateName);                 //Node in Graph
        graph.getNodes().forEach((a,b)->{
            assertTrue(a.getGraph()==graph);
            assertTrue(graph.getGroup().getChildren().contains(b.getFullNode()));                     //Shape in Graph
        });
    }

    @Test
    public void SimpleFullyConnectTest(){
        LinkedList<Junction> j = new LinkedList<>();
        //LinkedList<Node> nodes = new LinkedList<>();
        HashSet<Node> nodes = new HashSet<>(128);
        HashSet<Node> checkedNodes = new HashSet<>(64);

        Junction.clearSelection();
        int n = addNodes(5);                                     //add n nodes
        for(GraphShape junction : graph.getNodes().values()){
            j.add((Junction)junction);
        }
        //n = j.size();
        int k =0;
        if (n!=0){
            k = n;                                              //connect k=n nodes
            for (int i=0; i<k; i++){
            j.get(i).addToSelection();
            nodes.add(j.get(i).getRepresentedObjects().get(0));
            }
        }
        graph.fullyConnect(Junction.getSelection());
        assertTrue (graph.getEdges().size()== k*(k-1)/2);

        Junction.getSelection().forEach((a)->{
           for(Edge edge : a.getRepresentedObjects().get(0).getEdges()){
               Node otherNode=null;
               if(edge.getNode1()==a.getRepresentedObjects().get(0)){otherNode=edge.getOtherNode(edge.getNode1());}
               if(edge.getNode2()==a.getRepresentedObjects().get(0)){otherNode=edge.getOtherNode(edge.getNode2());}
               assertTrue(edge.getGraph()==graph);
               assertTrue(graph.getEdges().keySet().contains(edge));
               assertTrue(graph.getGroup().getChildren().contains(graph.getEdges().get(edge).getFullNode()));
               assertTrue(otherNode!=null);
               assertTrue(nodes.contains(otherNode));
               assertFalse(checkedNodes.contains(otherNode));
               checkedNodes.add(otherNode);
           }
           checkedNodes.clear();
        });
        k=graph.getEdges().size();
        graph.fullyConnect(Junction.getSelection());
        assertTrue(k==graph.getEdges().size());
    }
    //TODO
    @Test
    public void SimpleAddElementTest(){
        addNodes(5);
        addEdges(5);
        LinkedList<Node> nodes= new LinkedList<>();
        nodes.addAll(graph.getNodes().keySet());
        Element e;
        if (nodes.size() > 0) {
            e = Element.in(graph.getContext()).create("Test", Element.Type.SwWechsel, nodes.get(0), Element.State.NOSIG);
            graph.addElement(e);
            assertTrue(graph.getElements().containsKey(e));
            assertTrue(e.getGraph()==graph);
            assertTrue(graph.getGroup().getChildren().contains(graph.getElements().get(e).getFullNode()));
        }
    }

    @Test
    public void SimpleRemoveElementTest(){
        addNodes(5);
        addEdges(5);
        LinkedList<Node> nodes= new LinkedList<>();
        nodes.addAll(graph.getNodes().keySet());
        Element e;
        if (nodes.size() > 0) {
            e = Element.in(graph.getContext()).create("Test", Element.Type.SwWechsel, nodes.get(0), Element.State.NOSIG);
            WeakReference<Element> eR= new WeakReference<Element>(e);
            graph.addElement(e);
            graph.removeElement(e);
            Junction.clearSelection();
            e=null;
            System.gc();

            assertNull(eR.get());

        }


    }

//TODO
    @Test
    public void SimpleRemoveNodeTest(){
        LinkedList<Node> nodes= new LinkedList<>();
        LinkedList<Edge> edges= new LinkedList<>();
        addNodes(5);
        addEdges(5);
        nodes.addAll(graph.getNodes().keySet());
        Node removedNode = nodes.get(0);
        edges.addAll(removedNode.getEdges());
        Element e = Element.in(graph.getContext()).create("Test", Element.Type.SwWechsel, removedNode, Element.State.NOSIG);
        Element e2 = Element.in(graph.getContext()).create("TestDirection", Element.Type.SwWechsel, nodes.get(1), Element.State.NOSIG);
        LogicalGroup TestGroup =LogicalGroup.GroupFactory.create("TestGroup", "???");
        TestGroup.addElement(e);
        e2.setDirection(removedNode);
        graph.addElement(e2);
        graph.addElement(e);
        WeakReference<Element> eR= new WeakReference<Element>(e);
        WeakReference<Edge> edgeR =new WeakReference<Edge>(edges.get(0));
        WeakReference<Node> nodeR = new WeakReference<Node>(removedNode);
        assertTrue(eR.get()!=null && edgeR.get()!=null && nodeR.get()!=null);
        nodes.clear();
        edges.clear();
        graph.removeNode(removedNode);
        removedNode=null;
        e=null;
        Junction.clearSelection();
        System.gc();
        assertNull(eR.get());
        assertNull(nodeR.get());
        assertNull(edgeR.get());

    }

    @Test
    public void disconnectTest() {
        int n = addNodes(5);
        addEdges(5);
        LinkedList<Node> nodes = new LinkedList<>();
        LinkedList<Edge> edges = new LinkedList<>();
        WeakReference<Edge> weakEdge=null;
        WeakReference<GraphShape> EdgeShape=null;
        nodes.addAll(graph.getNodes().keySet());
        //System.out.println(nodes.size());
        if (nodes.size() > 0) {
            //System.out.println(graph.getEdges().size());
            //System.out.println(nodes.get(0).getEdges().size());
            edges.addAll(nodes.get(0).getEdges());
            if(edges.size()>0){
                weakEdge= new WeakReference<>(edges.get(0));
                EdgeShape= new WeakReference<GraphShape>(graph.getEdges().get(edges.get(0)));
            }
            graph.disconnect(nodes.get(0));
            //System.out.println(graph.getEdges().size());

            edges.forEach((a)->{assertFalse(graph.getEdges().containsKey(nodes.get(0).getEdges()));});
            assertTrue(nodes.get(0).getEdges().size()==0);
        }
        edges.clear();
        System.gc();
        if (weakEdge!=null){
            assertNull(weakEdge.get());
            assertNull(EdgeShape.get());
        }
    }

    public void addEdges(int k){
        LinkedList<Junction> j = new LinkedList<>();
        Junction.clearSelection();
        for(GraphShape junction : graph.getNodes().values()){
            j.add((Junction)junction);
        }
        if (j.size()!=0 && k-1<j.size()){
            for (int i=0; i<k; i++){
                j.get(i).addToSelection();
            }
        }
        graph.fullyConnect(Junction.getSelection());
    }

    public  int addNodes(int n) {

        for (int i = 0; i < n; i++) {
            String name = String.valueOf(i);
            if (!Node.in(graph.getContext()).NameExists(name)) {
                graph.addNode(name, new Coordinates(i, i));
            }
        }
        return n;
    }

}
