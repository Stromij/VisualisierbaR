package com.github.bachelorpraktikum.dbvisualization.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.logging.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Represents a train.<br> There will always be exactly one instance of this class per name per
 * {@link Context}. <p>Once {@link EventFactory#init(Edge) initialized}, a train has exactly one
 * {@link #getState(int) state} at any given point of time (represented by a positive integer).</p>
 * <p>Only the state at a point of time after or at the time of the last registered event can
 * change.</p> <p>{@link EventFactory#terminate(int, int) Terminated} trains are immutable.</p>
 */
@ParametersAreNonnullByDefault
public class Train {
    private static final Logger log = Logger.getLogger(Train.class.getName());

    @Nonnull
    private final String name;
    @Nonnull
    private final String readableName;
    private final int length;

    @Nonnull
    private final ObservableList<TrainEvent> events;

    private Train(String name, String readableName, int length) {
        this.name = Objects.requireNonNull(name);
        this.readableName = Objects.requireNonNull(readableName);
        if (length <= 0) {
            throw new IllegalArgumentException("length must be greater than 0");
        }
        this.length = length;

        events = FXCollections.observableArrayList();
    }

    /**
     * Manages instances of {@link Train}.<br>
     * Ensures that there will always be only one instance of Train per name per context.
     */
    @ParametersAreNonnullByDefault
    public static final class Factory {
        private static final Map<Context, Factory> instances = new WeakHashMap<>();

        @Nonnull
        private final Map<String, Train> trains;

        @Nonnull
        private static Factory getInstance(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }
            return instances.computeIfAbsent(context, g -> new Factory());
        }

        private Factory() {
            this.trains = new HashMap<>();
        }

        /**
         * Potentially creates a new train instance.
         *
         * @param name         unique name of the train
         * @param readableName human readable name of the train
         * @param length       length of the train in meters
         * @return an instance of {@link Train}
         * @throws IllegalArgumentException if another instance with the same name but <b>with a
         *                                  different readableName or length.</b> exists
         * @throws IllegalArgumentException if length is negative or zero
         * @throws NullPointerException     if name or readableName is null
         */
        @Nonnull
        public Train create(String name, String readableName, int length) {
            Train result = trains.get(Objects.requireNonNull(name));
            if (result == null) {
                result = new Train(name, readableName, length);
                trains.put(name, result);
            } else if (result.getLength() != length
                    || !result.getReadableName().equals(readableName)) {
                throw new IllegalArgumentException("train already exists, but differently");
            }

            return result;
        }

        /**
         * Gets the only {@link Train} instance for the given name.
         *
         * @param name the train's name
         * @return the train instance for the given name
         * @throws NullPointerException     if name is null
         * @throws IllegalArgumentException if there is no train with the given name in this
         *                                  context
         */
        @Nonnull
        public Train get(String name) {
            Train train = trains.get(Objects.requireNonNull(name));
            if (train == null) {
                throw new IllegalArgumentException("unknown train: " + name);
            }
            return train;
        }

        /**
         * Gets all trains in this {@link Context}.
         *
         * @return an unmodifiable collection of {@link Train} instances
         */
        @Nonnull
        public Collection<Train> getAll() {
            return Collections.unmodifiableCollection(trains.values());
        }
    }

    /**
     * Gets the {@link Factory} instance for the given context.
     *
     * @param context the context
     * @return the only Factory instance for this context
     * @throws NullPointerException if context is null
     */
    @Nonnull
    public static Factory in(Context context) {
        return Factory.getInstance(context);
    }

    /**
     * Gets the unique name of this {@link Train}.
     *
     * @return the name
     */
    @Nonnull
    public String getName() {
        return name;
    }

    /**
     * Gets the human readable name of this {@link Train}.
     *
     * @return the human readable name
     */
    @Nonnull
    public String getReadableName() {
        return readableName;
    }

    /**
     * Gets the length of this {@link Train} in meters.
     *
     * @return the length in meters
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets an unmodifiable list of all known events of this train.
     * The list will get updated if a new event is appended to the end.
     *
     * @return the list of events
     */
    public ObservableList<? extends Event> getEvents() {
        return FXCollections.unmodifiableObservableList(events);
    }

    /**
     * Gets the state of this {@link Train} at the given time.
     * {@link State} objects are immutable.
     *
     * @param time the time in milliseconds since the start of the simulation
     * @return the state of the train at the given point in time.
     * @throws IllegalArgumentException if time is negative
     * @throws IllegalStateException    if this train has not been {@link EventFactory#init(Edge)
     *                                  initialized}
     */
    @Nonnull
    public State getState(int time) {
        return getState(time, 0);
    }

    /**
     * Gets the state of this train at the given time, similar to {@link #getState(int)}.<br>In
     * contrast to the aforementioned method, this method can use the given state as a starting
     * point for searching the wanted state, which is potentially more efficient. If the time of the
     * given starting state is after the wanted time, this method won't improve performance.
     *
     * @param time   the time in milliseconds since the start of the simulation
     * @param before the state to use as a jumping off point
     * @return the state of the train at the given point in time.
     * @throws IllegalArgumentException if time is negative
     * @throws IllegalArgumentException if before is a state of a different train
     * @throws NullPointerException     if before is null
     * @throws IllegalStateException    if this train has not been {@link EventFactory#init(Edge)
     *                                  initialized}
     */
    @Nonnull
    public State getState(int time, State before) {
        if (before == null) {
            throw new NullPointerException("before state is null");
        }

        if (!before.getTrain().equals(this)) {
            throw new IllegalArgumentException("passed state of different train");
        }

        if (before.getTime() > time) {
            return getState(time);
        }

        if (before instanceof InterpolatableState) {
            return getState(time, ((InterpolatableState) before).getIndex());
        }

        return getState(time);
    }

    @Nonnull
    private State getState(int time, int startingIndex) {
        if (time < 0) {
            throw new IllegalArgumentException("time is negative");
        }

        if (events.isEmpty()) {
            throw new IllegalStateException("not initialized");
        }

        Iterator<TrainEvent> iterator = events.listIterator(startingIndex);
        TrainEvent result = iterator.next();

        while (iterator.hasNext()) {
            TrainEvent event = iterator.next();
            if (event.getTime() > time) {
                return result.getState().interpolate(time, event.getState());
            }
            result = event;
        }

        return result.getState();
    }

    /**
     * Gets the {@link EventFactory} for this train.
     *
     * @return the EventFactory
     */
    @Nonnull
    public EventFactory eventFactory() {
        return new EventFactory();
    }

    @Override
    public String toString() {
        return "Train{"
                + "name='" + name + '\''
                + ", readableName='" + readableName + '\''
                + ", length=" + length
                + '}';
    }

    @FunctionalInterface
    private interface EventCreator {
        @Nonnull
        TrainEvent create(TrainEvent before);
    }

    /**
     * Creates events for an instance of {@link Train}.
     */
    @ParametersAreNonnullByDefault
    public final class EventFactory {
        private EventFactory() {
        }

        /**
         * Initializes the {@link Train} instance corresponding to this factory.
         * The back of the train will be at totalDistance 0 from the start of the edge.
         *
         * @param edge the edge on which this train should be initialized.
         * @throws IllegalStateException if this method is called twice for the same train.
         */
        public void init(Edge edge) {
            if (!events.isEmpty()) {
                throw new IllegalStateException("already initialized");
            }
            events.add(new InitEvent(Train.this, edge));
        }

        private void addState(int time, EventCreator creator) {
            if (events.isEmpty()) {
                throw new IllegalStateException("not initialized");
            }
            TrainEvent before = events.get(events.size() - 1);
            if (before instanceof TerminateEvent) {
                throw new IllegalStateException("already terminated");
            }
            if (before.getTime() > time) {
                throw new IllegalStateException("tried to insert event before last element");
            }
            events.add(creator.create(before));
        }

        /**
         * Registers a speed event.
         *
         * @param time       the time of the event
         * @param distance   the totalDistance travelled since the last event
         * @param speedAfter the speed at this point of time
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void speed(int time, int distance, int speedAfter) {
            addState(time, (before) -> new SpeedEvent(before, time, distance, speedAfter));
        }

        /**
         * Registers a reachStart event.<br> After this event the front of the train will be at
         * totalDistance 0 from the start of the given {@link Edge}.
         *
         * @param time     the time of the event
         * @param edge     the edge the train reached
         * @param distance the totalDistance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void reach(int time, Edge edge, int distance) {
            addState(time, (before) -> new ReachEvent(before, time, distance, edge));
        }

        /**
         * Registers a leave event.<br> After this event the back of the train will be at
         * totalDistance 0 from the start of the given {@link Edge}.
         *
         * @param time     the time of the event
         * @param edge     the edge the train reached
         * @param distance the totalDistance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void leave(int time, Edge edge, int distance) {
            addState(time, (before) -> new LeaveEvent(before, time, distance, edge));
        }

        /**
         * Terminates this train. Can only be called once.
         *
         * @param time     the time of the event
         * @param distance the totalDistance travelled since the last event
         * @throws IllegalStateException if the train has not been initialized
         * @throws IllegalStateException if the train has already been terminated
         * @throws IllegalStateException if the specified time lies before the time of the last
         *                               event
         */
        public void terminate(int time, int distance) {
            addState(time, (before) -> new TerminateEvent(before, time, distance));
        }
    }

    /**
     * Represents the state of a {@link Train} at a specific point of time.
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public interface State extends Comparable<State> {
        @Nonnull
        Train getTrain();

        int getTime();

        boolean isTerminated();

        int getSpeed();

        @Nonnull
        Position getPosition();

        /**
         * Gets the total totalDistance the train has travelled at this point.
         *
         * @return the totalDistance in meters
         */
        int getTotalDistance();

        @Override
        default int compareTo(State other) {
            return Integer.compare(getTime(), other.getTime());
        }
    }

    /**
     * Represents the position of a {@link Train}.
     */
    @Immutable
    @ParametersAreNonnullByDefault
    public interface Position {
        /**
         * Gets the {@link Train} corresponding to this {@link Position}.
         *
         * @return the train
         */
        @Nonnull
        Train getTrain();

        /**
         * Gets the {@link Edge} the front of the {@link Train} is on.
         *
         * @return the edge
         */
        @Nonnull
        Edge getFrontEdge();

        /**
         * Gets the totalDistance the front of the train has travelled on the {@link #getFrontEdge()
         * front edge}.
         *
         * @return the totalDistance in meters
         */
        int getFrontDistance();

        /**
         * Gets the {@link Edge} the back of the {@link Train} is on.
         *
         * @return the edge
         */
        @Nonnull
        Edge getBackEdge();

        /**
         * Gets the totalDistance the back of the train is away from the end of the {@link
         * #getBackEdge() back edge}.
         *
         * @return the totalDistance in meters
         */
        int getBackDistance();

        @Nonnull
        List<Edge> getEdges();
    }

    private abstract static class TrainEvent implements Event {
        private final int index;
        @Nonnull
        private final Train train;
        private final int time;
        private final int distance;
        private final int totalDistance;
        @Nonnull
        private final TrainPosition position;

        TrainEvent(int index, @Nonnull Train train, int time, int distance, int totalDistance, @Nonnull TrainPosition position) {
            this.index = index;
            this.train = train;
            this.time = time;
            this.distance = distance;
            this.totalDistance = totalDistance;
            this.position = position;
        }

        final int getIndex() {
            return index;
        }

        @Nonnull
        final Train getTrain() {
            return train;
        }

        @Override
        public final int getTime() {
            return time;
        }

        final int getDistance() {
            return distance;
        }

        final int getTotalDistance() {
            return totalDistance;
        }

        @Nonnull
        final TrainPosition getPosition() {
            return position;
        }

        @Nonnull
        InterpolatableState.Builder stateBuilder() {
            return new InterpolatableState.Builder(getTrain())
                    .index(getIndex())
                    .time(getTime())
                    .distance(getTotalDistance())
                    .position(getPosition());
        }

        /**
         * Gets the state of the train after this event.
         * This method may return different states for consecutive calls.
         *
         * @return the state
         */
        @Nonnull
        final InterpolatableState getState() {
            return stateBuilder().build();
        }
    }

    private static class SpeedEvent extends TrainEvent {
        private final int speed;

        SpeedEvent(int index, @Nonnull Train train, int time, int distance, int totalDistance, @Nonnull TrainPosition position, int speed) {
            super(index, train, time, distance, totalDistance, position);
            this.speed = speed;
        }

        SpeedEvent(TrainEvent before, int time, int distance, int speed) {
            this(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    before.getPosition().move(distance),
                    speed
            );
        }

        final int getSpeed() {
            return speed;
        }

        @Nonnull
        @Override
        InterpolatableState.Builder stateBuilder() {
            return super.stateBuilder()
                    .speed(getSpeed());
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Speed{"
                    + "time=" + getTime()
                    + ", totalDistance=" + getTotalDistance()
                    + ", speed=" + getSpeed()
                    + "}";
        }
    }

    private static class InitEvent extends SpeedEvent {
        InitEvent(@Nonnull Train train, Edge startEdge) {
            super(0, train, 0, 0, 0, TrainPosition.init(train, startEdge), 0);
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Init{"
                    + "startEdge=" + getPosition().getBackEdge().getName()
                    + "}";
        }
    }

    private static class TerminateEvent extends PositionEvent {
        TerminateEvent(TrainEvent before, int time, int distance) {
            super(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    before.getPosition().move(distance)
            );
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Terminate{"
                    + "time=" + getTime()
                    + ", totalDistance=" + getTotalDistance()
                    + "}";
        }

        @Nonnull
        @Override
        InterpolatableState.Builder stateBuilder() {
            return super.stateBuilder()
                    .terminated(true);
        }
    }

    private abstract static class PositionEvent extends TrainEvent {

        PositionEvent(int index, @Nonnull Train train, int time, int distance, int totalDistance, @Nonnull TrainPosition position) {
            super(index, train, time, distance, totalDistance, position);
        }

        @Nonnull
        @Override
        InterpolatableState.Builder stateBuilder() {
            return super.stateBuilder()
                    .speed(calculateSpeed());
        }

        @Nonnull
        private SpeedEvent getSpeedEventBefore() {
            for (int index = getIndex() - 1; index >= 0; index--) {
                TrainEvent event = getTrain().events.get(index);
                if (event instanceof SpeedEvent) {
                    return (SpeedEvent) event;
                }
            }
            throw new IllegalStateException("Missing init event");
        }

        @Nullable
        private SpeedEvent getSpeedEventAfter() {
            List<TrainEvent> events = getTrain().events;
            for (int index = getIndex() + 1; index < events.size(); index++) {
                TrainEvent event = events.get(index);
                if (event instanceof SpeedEvent) {
                    return (SpeedEvent) event;
                }
            }
            return null;
        }

        private int calculateSpeed() {
            SpeedEvent before = getSpeedEventBefore();
            SpeedEvent after = getSpeedEventAfter();

            if (after == null) {
                return before.getSpeed();
            }

            int relativeAfterTime = after.getTime() - before.getTime();
            int relativeTime = getTime() - before.getTime();

            int interpolatedSpeed = before.getSpeed();
            int speedDiff = after.getSpeed() - before.getSpeed();
            interpolatedSpeed += (int) (((double) speedDiff) / relativeAfterTime * relativeTime);
            return interpolatedSpeed;
        }
    }

    private static class ReachEvent extends PositionEvent {
        @Nonnull
        private final Edge reached;

        ReachEvent(TrainEvent before, int time, int distance, Edge reached) {
            super(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    before.getPosition().reachFront(reached));
            this.reached = reached;
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Reach{"
                    + "time=" + getTime()
                    + ", distance=" + getDistance()
                    + ", reached=" + reached.getName()
                    + "}";
        }
    }

    private static class LeaveEvent extends PositionEvent {
        @Nonnull
        private final Edge left;

        LeaveEvent(TrainEvent before, int time, int distance, Edge left) {
            super(before.getIndex() + 1,
                    before.getTrain(),
                    time,
                    distance,
                    before.getTotalDistance() + distance,
                    before.getPosition().leaveBack(left, distance));
            this.left = left;
        }

        @Nonnull
        @Override
        public String getDescription() {
            return getTrain().getReadableName() + ": Leave{"
                    + "time=" + getTime()
                    + ", distance=" + getDistance()
                    + ", left=" + left.getName()
                    + "}";
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    private static final class InterpolatableState implements State {
        @Nonnull
        private final Train train;
        private final int index;
        private final int time;
        private final int distance;
        @Nonnull
        private final TrainPosition position;
        private final int speed;
        private final boolean terminated;

        private InterpolatableState(Train train,
                                    int index,
                                    boolean terminated,
                                    int time,
                                    int distance,
                                    TrainPosition position,
                                    int speed) {
            this.train = Objects.requireNonNull(train);
            if (index < 0) {
                throw new IllegalArgumentException("index is negative");
            }
            this.index = index;
            this.terminated = terminated;
            if (time < 0) {
                throw new IllegalArgumentException("time is negative");
            }
            this.time = time;
            if (distance < 0) {
                throw new IllegalArgumentException("distance is negative");
            }
            this.distance = distance;
            this.position = Objects.requireNonNull(position);
            if (speed < 0) {
                throw new IllegalArgumentException(String.format("speed (%d) is negative", speed));
            }
            this.speed = speed;
        }

        @ParametersAreNonnullByDefault
        static class Builder {
            @Nonnull
            private final Train train;

            private int time = -1;
            private int speed = -1;
            private boolean terminated = false;
            private int index = -1;
            private int distance = -1;
            private TrainPosition position = null;

            Builder(Train train) {
                this.train = train;
            }

            Builder time(int time) {
                this.time = time;
                return this;
            }

            Builder speed(int speed) {
                this.speed = speed;
                return this;
            }

            Builder terminated(boolean terminated) {
                this.terminated = terminated;
                return this;
            }

            Builder index(int index) {
                this.index = index;
                return this;
            }

            Builder distance(int distance) {
                this.distance = distance;
                return this;
            }

            Builder position(TrainPosition position) {
                this.position = position;
                return this;
            }


            InterpolatableState build() {
                return new InterpolatableState(train, index, terminated, time, distance, position, speed);
            }
        }

        @Nonnull
        final InterpolatableState interpolate(int targetTime, InterpolatableState other) {
            if (compareTo(other) > 0) {
                return other.interpolate(targetTime, this);
            }

            if (getTime() > targetTime) {
                throw new IllegalArgumentException("time not between states");
            }

            int relativeTargetTime = targetTime - getTime();
            int relativeOtherTime = other.getTime() - getTime();

            int interpolatedSpeed = getSpeed();
            // interpolate speed
            if (!other.isTerminated() && getSpeed() != other.getSpeed()) {
                int speedDiff = other.getSpeed() - getSpeed();
                interpolatedSpeed += (int) (((double) speedDiff) / relativeOtherTime * relativeTargetTime);
            }

            int interpolatedDistance = getTotalDistance();
            TrainPosition interpolatedPosition = getPosition();
            if (!getPosition().equals(other.getPosition())) {
                int distanceDiff = other.getTotalDistance() - getTotalDistance();
                int interpolationDistance = (int) (((double) distanceDiff) / relativeOtherTime * relativeTargetTime);
                interpolatedDistance += interpolationDistance;
                interpolatedPosition = interpolatedPosition.interpolationMove(interpolationDistance, other.getPosition().getFrontEdge());
            }


            return new Builder(getTrain())
                    .index(getIndex())
                    .time(targetTime)
                    .distance(interpolatedDistance)
                    .speed(interpolatedSpeed)
                    .position(interpolatedPosition)
                    .build();
        }

        @Nonnull
        @Override
        public Train getTrain() {
            return train;
        }

        @Nonnull
        @Override
        public TrainPosition getPosition() {
            return position;
        }

        @Override
        public int getTime() {
            return time;
        }

        @Override
        public int getTotalDistance() {
            return distance;
        }

        public int getIndex() {
            return index;
        }

        @Override
        public int getSpeed() {
            return speed;
        }

        @Override
        public boolean isTerminated() {
            return terminated;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof InterpolatableState)) return false;

            InterpolatableState that = (InterpolatableState) obj;

            if (time != that.time) return false;
            if (getSpeed() != that.getSpeed()) return false;
            if (!train.equals(that.train)) return false;
            return getPosition().equals(that.getPosition());
        }

        @Override
        public int hashCode() {
            int result = train.hashCode();
            result = 31 * result + time;
            result = 31 * result + getSpeed();
            result = 31 * result + getPosition().hashCode();
            return result;
        }
    }

    @Immutable
    @ParametersAreNonnullByDefault
    private static final class TrainPosition implements Position {
        private final Train train;
        private final LinkedList<Edge> edges;
        private final int frontDistance;
        private final int backDistance;

        private TrainPosition(Train train, LinkedList<Edge> edges, int frontDistance) {
            this.train = train;
            this.edges = edges;
            this.frontDistance = frontDistance;

            int trainLength = train.getLength() - getFrontDistance();
            int backDistance = 0;
            Iterator<Edge> iterator = edges.listIterator(1);
            Edge edge;
            while (iterator.hasNext()) {
                edge = iterator.next();
                if (trainLength == 0) {
                    iterator.remove();
                } else if (trainLength < edge.getLength()) {
                    backDistance = trainLength;
                    trainLength = 0;
                } else {
                    trainLength -= edge.getLength();
                }
            }

            this.backDistance = backDistance;
        }

        static TrainPosition init(Train train, Edge edge) {
            LinkedList<Edge> edges = new LinkedList<>();
            edges.add(edge);
            return new TrainPosition(train, edges, Math.min(train.getLength(), edge.getLength()));
        }

        @Nonnull
        @Override
        public Train getTrain() {
            return train;
        }

        @Nonnull
        @Override
        public Edge getFrontEdge() {
            return edges.getFirst();
        }

        @Override
        public int getFrontDistance() {
            return frontDistance;
        }

        @Nonnull
        @Override
        public Edge getBackEdge() {
            return edges.getLast();
        }

        @Override
        public int getBackDistance() {
            return backDistance;
        }

        @Nonnull
        @Override
        public List<Edge> getEdges() {
            return Collections.unmodifiableList(edges);
        }

        @Nonnull
        TrainPosition move(int distance) {
            int newDistance = getFrontDistance() + distance;
            return new TrainPosition(getTrain(), new LinkedList<>(edges), newDistance);
        }

        @Nonnull
        TrainPosition leaveBack(Edge newBack, int movedDistance) {
            LinkedList<Edge> edges = new LinkedList<>(getEdges());
            edges.add(newBack);
            return new TrainPosition(getTrain(), edges, getFrontDistance() + movedDistance);
        }

        @Nonnull
        TrainPosition reachFront(Edge newStart) {
            LinkedList<Edge> edges = new LinkedList<>(getEdges());
            edges.addFirst(newStart);
            return new TrainPosition(getTrain(), edges, 0);
        }

        @Nonnull
        TrainPosition interpolationMove(int moveDistance, Edge possibleNewStart) {
            LinkedList<Edge> edges = new LinkedList<>(getEdges());
            int newDistance = getFrontDistance() + moveDistance;
            if (newDistance > getFrontEdge().getLength()) {
                edges.addFirst(possibleNewStart);
                newDistance -= getFrontEdge().getLength();
            }

            return new TrainPosition(getTrain(), edges, newDistance);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Train.Position)) return false;

            Train.Position that = (Train.Position) obj;

            if (!train.equals(that.getTrain())) return false;
            if (frontDistance != that.getFrontDistance()) return false;
            if (!getFrontEdge().equals(that.getFrontEdge())) return false;
            if (backDistance != that.getBackDistance()) return false;
            return getBackEdge().equals(that.getBackEdge());
        }

        @Override
        public int hashCode() {
            int result = getFrontEdge().hashCode();
            result = 31 * result + frontDistance;
            return result;
        }

        @Override
        public String toString() {
            return "Position{"
                    + "edge=" + getFrontEdge()
                    + ", frontDistance=" + frontDistance
                    + '}';
        }
    }
}
