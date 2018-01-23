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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

public class GraphTest   {

    Graph graph;
    Random cordGen = new Random();
    RandomString nameGen = new RandomString(1, ThreadLocalRandom.current());




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
    public void addNodeTest(){
        int duplicateName = 0;
        int n = cordGen.nextInt(100);
        for (int i=0; i<n; i++){
            String name= nameGen.nextString();
            if(Node.in(graph.getContext()).NameExists(name)) duplicateName++;
            try{graph.addNode(name, new Coordinates(cordGen.nextInt(100), cordGen.nextInt(100)));}
            catch (IllegalArgumentException e) {assertTrue (duplicateName>0);}

            assertTrue (Node.in(graph.getContext()).NameExists(name)==true);                                        //Node in Factory
        }
        assertTrue (graph.getNodes().size()== n-duplicateName);                                                     //Node in Graph
    }

    @Test
    public void fullyconnectTest(){
        LinkedList<Junction> j = new LinkedList<>();
        //LinkedList<Node> nodes = new LinkedList<>();
        HashSet<Node> nodes = new HashSet<>(128);
        HashSet<Node> checkedNodes = new HashSet<>(64);

        Junction.clearSelection();
        int n = addRandomNodes();
        for(GraphShape junction : graph.getNodes().values()){
            j.add((Junction)junction);
        }
        n = j.size();
        int k =0;
        if (n!=0){
            k = cordGen.nextInt(Math.abs(n));
            for (int i=0; i<k; i++){
            int l = cordGen.nextInt(Math.abs(n));
            j.get(l).addToSelection();
            nodes.add(j.get(l).getRepresentedObjects().get(0));
            }
        }
        graph.fullyConnect(Junction.getSelection());
        k=Junction.getSelection().size();
        //System.out.println(k);
        //System.out.println(graph.getEdges().size());
        assertTrue (graph.getEdges().size()== k*(k-1)/2);

        Junction.getSelection().forEach((a)->{
           for(Edge edge : a.getRepresentedObjects().get(0).getEdges()){
               Node otherNode=null;
               if(edge.getNode1()==a.getRepresentedObjects().get(0)){otherNode=edge.getOtherNode(edge.getNode1());}
               if(edge.getNode2()==a.getRepresentedObjects().get(0)){otherNode=edge.getOtherNode(edge.getNode2());}

               assertTrue(otherNode!=null);
               assertTrue(nodes.contains(otherNode));
               assertFalse(checkedNodes.contains(otherNode));
               checkedNodes.add(otherNode);
           }
           checkedNodes.clear();
        });
    }
    //TODO
    @Test
    public void addElementTest(){
        addRandomNodes();
        addRandomEdges();

    }
//TODO
    @Test
    public void removeNodeTest(){
        addRandomNodes();
        addRandomEdges();
    }

    @Test
    public void disconnectTest() {
        int n = addRandomNodes();
        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addAll(graph.getNodes().keySet());
        //System.out.println(nodes.size());
        if (nodes.size() > 0) {
            addRandomEdges();
            //System.out.println(graph.getEdges().size());
            //System.out.println(nodes.get(0).getEdges().size());
            graph.disconnect(nodes.get(0));
            //System.out.println(graph.getEdges().size());

            nodes.get(0).getEdges().forEach((a)->{assertFalse(graph.getEdges().containsKey(nodes.get(0).getEdges()));});
        }

    }

    public int addRandomEdges(){
        Junction.clearSelection();
        LinkedList<Node> nodes = new LinkedList<>();
        nodes.addAll(graph.getNodes().keySet());
        if (nodes.size()==0) return 0;
        int k = cordGen.nextInt(nodes.size());
        for (int i=0; i<k; i++) {
            int a = cordGen.nextInt(nodes.size());
            int b = cordGen.nextInt(nodes.size());
            if (a != b){
                ((Junction) graph.getNodes().get(nodes.get(a))).addToSelection();
                ((Junction) graph.getNodes().get(nodes.get(b))).addToSelection();
                graph.fullyConnect(Junction.getSelection());
                Junction.clearSelection();
            }
            else i--;
        }
        return k;
    }

    public int addRandomNodes(){
        int n = cordGen.nextInt(100);
        int duplicates=0;
        for (int i=0; i<n; i++){
            String name= nameGen.nextString();

                if (!Node.in(graph.getContext()).NameExists(name)){
                    graph.addNode(name, new Coordinates(cordGen.nextInt(100), cordGen.nextInt(100)));}
                else duplicates++;
        }
        return n-duplicates;
    }



}
