package com.github.bachelorpraktikum.dbvisualization.model;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class TrainTest {
    private Context context;

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Before
    public void init() {
        context = new Context();
    }

    @Test
    public void testInstanceManager() {
        Train train = Train.in(context).create("t", "train", 100);
        assertSame(train, Train.in(context).get(train.getName()));
        assertSame(train, Train.in(context).create(train.getName(), train.getReadableName(), train.getLength()));
        assertTrue(Train.in(context).getAll().contains(train));
    }

    @Test
    public void testInstanceManagerInvalidName() {
        expected.expect(IllegalArgumentException.class);
        Train.in(context).get("t");
    }

    @Test
    public void testInstanceManagerExistsDifferentLength() {
        String name = "t";
        String readableName = "train";
        Train.in(context).create(name, readableName, 10);
        expected.expect(IllegalArgumentException.class);
        Train.in(context).create(name, readableName, 20);
    }

    @Test
    public void testInstanceManagerExistsDifferentReadableName() {
        String name = "t";
        String readableName = "train";
        Train.in(context).create(name, readableName, 10);
        expected.expect(IllegalArgumentException.class);
        Train.in(context).create(name, "trainz", 10);
    }

    @Test
    public void testName() {
        Train train = Train.in(context).create("t", "train", 100);
        assertEquals("t", train.getName());
    }

    @Test
    public void testReadableName() {
        Train train = Train.in(context).create("t", "train", 100);
        assertEquals("train", train.getReadableName());
    }

    @Test
    public void testLength() {
        Train train = Train.in(context).create("t", "train", 50);
        assertEquals(50, train.getLength());
    }

    @Test
    public void testGetStateNegativeTime() {
        Train train = Train.in(context).create("t", "train", 20);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 30, node1, node2);
        train.eventFactory().init(edge);

        expected.expect(IllegalArgumentException.class);
        train.getState(-1);
    }

    @Test
    public void testGetStateNotInitialized() {
        Train train = Train.in(context).create("t", "train", 20);
        expected.expect(IllegalStateException.class);
        train.getState(0);
    }

    @Test
    public void testInitStart() {
        Train train = Train.in(context).create("t", "train", 20);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 30, node1, node2);
        train.eventFactory().init(edge);

        Train.State state = train.getState(0);
        assertEquals(train.getLength(), state.getPosition().getFrontDistance());
        assertEquals(0, state.getPosition().getBackDistance());
        assertEquals(edge, state.getPosition().getFrontEdge());
        assertEquals(edge, state.getPosition().getBackEdge());
    }

    @Test
    public void testInitMiddle() {
        Train train = Train.in(context).create("t", "train", 20);

        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));
        Node node4 = Node.in(context).create("n4", new Coordinates(3, 0));

        Edge edge1 = Edge.in(context).create("e1", 30, node1, node2);
        Edge edge2 = Edge.in(context).create("e2", 40, node2, node3);
        Edge edge3 = Edge.in(context).create("e3", 50, node3, node4);

        train.eventFactory().init(edge2);

        Train.State state = train.getState(0);
        assertEquals(train.getLength(), state.getPosition().getFrontDistance());
        assertEquals(edge2, state.getPosition().getFrontEdge());
        assertEquals(0, state.getPosition().getBackDistance());
    }

    @Test
    public void testSpeed() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        train.eventFactory().init(edge);

        Train.State initState = train.getState(0);

        int time = 10;
        int distance = 20;
        int speed = 30;
        train.eventFactory().speed(time, distance, speed);

        assertEquals(initState, train.getState(0));

        Train.State state = train.getState(10);
        assertFalse(state.isTerminated());
        assertEquals(train, state.getTrain());
        assertEquals(time, state.getTime());
        assertEquals(edge, state.getPosition().getFrontEdge());
        assertEquals(train.getLength() + distance, state.getPosition().getFrontDistance());
        assertEquals(speed, state.getSpeed());

        assertEquals(state, train.getState(20));
    }

    @Test
    public void testReach() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));
        Edge edge1 = Edge.in(context).create("e1", 20, node1, node2);
        Edge edge2 = Edge.in(context).create("e2", 40, node2, node3);
        train.eventFactory().init(edge1);

        train.eventFactory().reach(10, edge2, 10);
        Train.State state = train.getState(10);
        assertEquals(10, state.getTime());
        assertEquals(0, state.getPosition().getFrontDistance());
        assertEquals(edge2, state.getPosition().getFrontEdge());

        assertEquals(edge1, state.getPosition().getBackEdge());
        assertEquals(10, state.getPosition().getBackDistance());
        assertFalse(state.isTerminated());
    }

    @Test
    public void testLeave() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));
        Edge edge1 = Edge.in(context).create("e1", 20, node1, node2);
        Edge edge2 = Edge.in(context).create("e2", 40, node2, node3);
        train.eventFactory().init(edge1);

        train.eventFactory().reach(10, edge2, 10);
        train.eventFactory().leave(20, edge1, 10);
        Train.State state = train.getState(20);
        assertEquals(20, state.getTime());
        assertEquals(0, state.getPosition().getBackDistance());
        assertEquals(edge2, state.getPosition().getBackEdge());

        assertEquals(edge2, state.getPosition().getFrontEdge());
        assertEquals(10, state.getPosition().getFrontDistance());
        assertFalse(state.isTerminated());
    }

    @Test
    public void testTerminate() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 20, node1, node2);
        train.eventFactory().init(edge);

        train.eventFactory().speed(5, 2, 20);
        train.eventFactory().terminate(10, 2);
        Train.State state = train.getState(10);
        assertEquals(10, state.getTime());
        assertTrue(state.isTerminated());
        assertEquals(14, state.getPosition().getFrontDistance());
        expected.expect(IllegalStateException.class);
        state.getSpeed();
    }

    @Test
    public void testSpeedInterpolation() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        train.eventFactory().init(edge);

        train.eventFactory().speed(10, 5, 10);
        train.eventFactory().speed(20, 5, 20);

        assertEquals(12, train.getState(12).getSpeed());
        assertEquals(15, train.getState(15).getSpeed());
        assertEquals(17, train.getState(17).getSpeed());
    }

    @Test
    public void testPositionInterpolation() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        train.eventFactory().init(edge);

        train.eventFactory().speed(10, 10, 5);

        assertEquals(11, train.getState(1).getPosition().getFrontDistance());
        assertEquals(13, train.getState(3).getPosition().getFrontDistance());
        assertEquals(15, train.getState(5).getPosition().getFrontDistance());
    }

    @Test
    public void testMergeEvents() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));
        Edge edge1 = Edge.in(context).create("e1", 20, node1, node2);
        Edge edge2 = Edge.in(context).create("e2", 20, node2, node3);
        train.eventFactory().init(edge1);

        train.eventFactory().reach(10, edge2, 10);
        train.eventFactory().speed(10, 5, 10);

        Train.State state = train.getState(10);
        assertEquals(5, state.getPosition().getFrontDistance());
        assertEquals(10, state.getSpeed());
    }

    @Test
    public void testInitAlreadyInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Node node3 = Node.in(context).create("n3", new Coordinates(2, 0));
        Edge edge1 = Edge.in(context).create("e1", 20, node1, node2);
        Edge edge2 = Edge.in(context).create("e2", 20, node2, node3);
        train.eventFactory().init(edge1);
        expected.expect(IllegalStateException.class);
        train.eventFactory().init(edge2);
    }

    @Test
    public void testSpeedNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        expected.expect(IllegalStateException.class);
        train.eventFactory().speed(10, 10, 10);
    }

    @Test
    public void testReachNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        expected.expect(IllegalStateException.class);
        train.eventFactory().reach(10, edge, 10);
    }

    @Test
    public void testLeaveNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        expected.expect(IllegalStateException.class);
        train.eventFactory().leave(10, edge, 10);
    }

    @Test
    public void testTerminateNotInitialized() {
        Train train = Train.in(context).create("t", "train", 10);
        expected.expect(IllegalStateException.class);
        train.eventFactory().terminate(10, 10);
    }

    @Test
    public void testSpeedAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);

        train.eventFactory().init(edge);
        train.eventFactory().terminate(5, 0);
        expected.expect(IllegalStateException.class);
        train.eventFactory().speed(10, 10, 10);
    }

    @Test
    public void testReachAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        train.eventFactory().init(edge);
        train.eventFactory().terminate(5, 0);
        expected.expect(IllegalStateException.class);
        train.eventFactory().reach(10, edge, 10);
    }

    @Test
    public void testLeaveAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        train.eventFactory().init(edge);
        train.eventFactory().terminate(5, 0);
        expected.expect(IllegalStateException.class);
        train.eventFactory().leave(10, edge, 10);
    }

    @Test
    public void testTerminateAlreadyTerminated() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        train.eventFactory().init(edge);
        train.eventFactory().terminate(5, 0);
        expected.expect(IllegalStateException.class);
        train.eventFactory().terminate(10, 10);
    }

    @Test
    public void testEventBeforeLast() {
        Train train = Train.in(context).create("t", "train", 10);
        Node node1 = Node.in(context).create("n1", new Coordinates(0, 0));
        Node node2 = Node.in(context).create("n2", new Coordinates(1, 0));
        Edge edge = Edge.in(context).create("e", 50, node1, node2);
        train.eventFactory().init(edge);
        train.eventFactory().speed(10, 10, 20);
        expected.expect(IllegalStateException.class);
        train.eventFactory().speed(5, 10, 10);
    }
}
