package com.github.bachelorpraktikum.visualisierbar.model;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.github.bachelorpraktikum.visualisierbar.view.graph.Graph;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents an element on the track.<p>Every element is associated with a {@link Node}.</p>
 * <p>Typically represents a signal. The {@link Type type} of signal can be retrieved via {@link
 * #getType()}</p>
 * <p>There is only one instance of Element per name per {@link Context}.</p>
 */
@ParametersAreNonnullByDefault
public final class Element implements GraphObject<Shape> {

    private static final Logger log = Logger.getLogger(Element.class.getName());
    @Nonnull
    private final ElementFactory factory;
    @Nonnull
    private String name;
    @Nonnull
    private final Node node;
    @Nonnull
    private final Type type;
    @Nullable
    private final Switch aSwitch;
    @Nonnull
    private final ReadOnlyObjectWrapper<State> stateProperty;
    @Nullable
    private Graph graph;

    /**
     * Represents the state of an {@link Element}.
     */
    public enum State {
        NOSIG(Color.BLACK), STOP(Color.RED), FAHRT(Color.BLUE);

        private final Color color;

        State(Color color) {
            this.color = color;
        }

        /**
         * The color an element with this state should be shown in.
         *
         * @return a color
         */
        public Color getColor() {
            return color;
        }

        /**
         * Gets the state with the given name.
         *
         * @param name the name
         * @return a state
         * @throws IllegalArgumentException if there is no state with that name
         * @throws NullPointerException if name is null
         */
        @Nonnull
        public static State fromName(String name) {
            return valueOf(name.trim().toUpperCase());
        }
    }

    /**
     * Represents the type of an {@link Element}.
     * Every type is associated with an image file containing the symbol for the element.
     */
    public enum Type implements Shapeable<Shape> {
        GeschwindigkeitsAnzeiger("GeschwindigkeitsAnzeigerImpl", () ->
            new Polygon(-1, 1, 1, 1, 0, -1)
        ),
        VorSignal("VorSignalImpl", "VorsignalImpl"),
        HauptSignal("HauptSignalImpl", "HauptsignalImpl"),
        GeschwindigkeitsVoranzeiger("GeschwindigkeitsVoranzeigerImpl", () ->
            new Polygon(-1, -1, 1, -1, 0, 1)
        ),
        SichtbarkeitsPunkt("SichtbarkeitsPunktImpl",
            "SichtbarkeitspunktImpl", "SichtbarkeitspunktImpl2"),
        GefahrenPunkt("GefahrenPunktImpl", "GefahrenpunktImpl"),
        Magnet("MagnetImpl", "MagnetImpl"),
        WeichenPunkt("WeichenPunktImpl", () ->
            new Polygon(0, 1, 2, 1, 2, 0)
        ),
        SwWechsel("SwWechselImpl",
            "SwWechselImpl", "SwWechselImpl2", "SwWechselImpl3", "SwWechselImpl4"),
        UnknownElement("", Rectangle::new);

        private final String logName;
        private final Property<VisibleState> stateProperty;
        private final Supplier<Shape> shapeSupplier;

        Type(String logName, String... imageNames) {
            this.logName = logName;
            this.stateProperty = new SimpleObjectProperty<>(VisibleState.AUTO);
            List<URL> imageUrls = new ArrayList<>(imageNames.length);

            for (String imageName : imageNames) {
                imageUrls
                    .add(Element.class.getResource(String.format("symbols/%s.fxml", imageName)));
            }

            this.shapeSupplier = () -> Shapeable.createShape(imageUrls);
        }

         public boolean isComposite(){

            if((this == Element.Type.GeschwindigkeitsAnzeiger) ||
                    (this== Element.Type.GeschwindigkeitsVoranzeiger)||
                    (this== Element.Type.VorSignal)||
                    (this== Element.Type.HauptSignal)) return true;
            return false;
        }

        Type(String logName, Supplier<Shape> shapeSupplier) {
            this.logName = logName;
            this.stateProperty = new SimpleObjectProperty<>(VisibleState.AUTO);
            this.shapeSupplier = shapeSupplier;
        }

        /**
         * Gets a potentially human readable name for this {@link Type}.
         *
         * @return the name of this type
         */
        @Nonnull
        public String getName() {
            return name();
        }

        public String getLogName() {
            return logName;
        }

        @Nonnull
        @Override
        public Shape createShape() {
            Shape shape = shapeSupplier.get();
            if (this == HauptSignal || this == VorSignal) {
                shape.setRotate(90);
            }
            if (this == Magnet) {
                shape.setRotate(180);
            }
            return shape;
        }

        @Nonnull
        @Override
        public Property<VisibleState> visibleStateProperty() {
            return stateProperty;
        }

        /**
         * Gets the {@link Type} corresponding to the given type name.
         * Falls back to UnknownElement.
         *
         * @param name the unique type name
         * @return a type
         */
        @Nonnull
        public static Type fromName(String name) {
            String lowerName = name.toLowerCase();
            Type longestMatch = UnknownElement;
            for (Type type : values()) {
                if (type.getLogName().length() >= longestMatch.getLogName().length()) {
                    String lowerTypeName = type.getLogName().toLowerCase();
                    if (lowerName.contains(lowerTypeName)) {
                        longestMatch = type;
                    }
                }
            }
            if (longestMatch == UnknownElement) {
                log.warning("Unknown element type: " + name);
            }
            return longestMatch;
        }
    }

    private Element(ElementFactory factory, String name, Type type, Node node, State state) {
        this.factory = Objects.requireNonNull(factory);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.node = Objects.requireNonNull(node);
        this.stateProperty = new ReadOnlyObjectWrapper<>(Objects.requireNonNull(state));
        this.graph=null;

        node.addElement(this);

        if (this.type == Type.WeichenPunkt) {
            this.aSwitch = factory.getSwitchFactory().create(this);
        } else {
            this.aSwitch = null;
        }

        // Add an event at time 0 with the initial state
        factory.addEvent(this, state, Context.INIT_STATE_TIME);
    }

    /**
     * <p>Manages all instances of {@link Element}. Ensures that is always only one instance of
     * element per name per {@link Context}.</p> <p>Additionally, provides the {@link #setTime(int)}
     * method to set the time of all Elements that exist in this factory's context.</p>
     */
    @ParametersAreNonnullByDefault
    public static final class ElementFactory implements Factory<Element> {

        private static final int INITIAL_ELEMENTS_CAPACITY = 256;
        private static final Map<Context, WeakReference<ElementFactory>> instances = new WeakHashMap<>();

        @Nonnull
        private final Map<String, Element> elements;

        @Nonnull
        private final Switch.Factory switchFactory;
        @Nonnull
        private final Factory<Node> nodeFactory;
        @Nonnull
        private final ObservableList<ElementEvent> events;
        private int currentTime;
        private int nextIndex;

        @Nonnull
        private static ElementFactory getInstance(Context context) {
            if (context == null) {
                throw new NullPointerException("context is null");
            }

            ElementFactory result = instances.computeIfAbsent(context, ctx -> {
                ElementFactory factory = new ElementFactory(ctx);
                ctx.addObject(factory);
                return new WeakReference<>(factory);
            }).get();

            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        }

        private ElementFactory(Context context) {
            this.elements = new LinkedHashMap<>(INITIAL_ELEMENTS_CAPACITY);

            this.switchFactory = Switch.in(context);
            this.nodeFactory = Node.in(context);
            this.events = FXCollections.observableArrayList();
            this.currentTime = -1;
            this.nextIndex = 0;

        }
        public boolean NameExists (@Nonnull String name){
            Element element = elements.get(Objects.requireNonNull(name));
            if (element == null) {
                return false;
            }
            return true;
        }


        @Nonnull
        private Switch.Factory getSwitchFactory() {
            return switchFactory;
        }

        /**
         * Potentially creates a new instance of {@link Element}.
         *
         * @param name the unique name of this element
         * @param type the {@link Type}
         * @param node the {@link Node} this element is located on
         * @param state the initial state of the element
         * @return an element
         * @throws NullPointerException if either of the arguments is null
         * @throws IllegalArgumentException if an element with the same name but different
         * parameters already exists
         * @throws IllegalArgumentException if the given node is from within another context
         */
        @Nonnull
        public Element create(String name, Type type, Node node, State state) {
            if (!nodeFactory.checkAffiliated(node)) {
                throw new IllegalArgumentException("Node is from the wrong context. " + node);
            }

            Element element = elements.computeIfAbsent(Objects.requireNonNull(name), elementName ->
                new Element(this, elementName, type, node, state)
            );
            State resultInitState = getStateAtTime(element, Context.INIT_STATE_TIME);
            if (!element.getName().equals(name)
                || !element.getType().equals(type)
                || !element.getNode().equals(node)
                || !resultInitState.equals(state)) {
                String elementFormat = "(type: %s, node: %s, initState: %s)";
                String message = "Element with name: %s already exists:\n"
                    + elementFormat + ", tried to recreate with following arguments:\n"
                    + elementFormat;
                message = String.format(message, name, type, node, state,
                    element.getType(), element.getNode(), resultInitState);
                throw new IllegalArgumentException(message);
            }
            return element;
        }

        public void remove (Element element){
            elements.remove(element.getName());
        }

        /**
         * Gets the state of an element at the given time, then resets the time to the previous
         * value.
         *
         * @param element the element
         * @param time the time to look up the state for
         * @return the state of the element at the given time
         */
        private State getStateAtTime(Element element, int time) {
            int resetTime = currentTime;
            setTime(time);
            State result = element.getState();
            setTime(resetTime);
            return result;
        }

        @Override
        @Nonnull
        public Element get(String name) {
            Element element = elements.get(Objects.requireNonNull(name));
            if (element == null) {
                throw new IllegalArgumentException("unknown element: " + name);
            }
            return element;
        }

        @Override
        @Nonnull
        public Collection<Element> getAll() {
            return Collections.unmodifiableCollection(elements.values());
        }

        @Override
        public boolean checkAffiliated(@Nonnull Element element) {
            return elements.get(element.getName()) == element;
        }

        private void addEvent(Element element, State state, int time) {
            addEvent(element, state, new LinkedList<>(), time);
        }

        private void addEvent(Element element, State state, List<String> warnings, int time) {
            if (!events.isEmpty() && time < events.get(events.size() - 1).getTime()) {
                warnings.add("tried to add before last event at " + time);
                time = events.get(events.size() - 1).getTime();
            }
            events.add(new ElementEvent(
                element, time, state, FXCollections.observableList(warnings)
            ));
            // maybe the states have to be updated
            if (time <= currentTime) {
                int refreshTime = currentTime;
                resetTime();
                setTime(refreshTime);
            }
        }

        private void resetTime() {
            currentTime = Context.INIT_STATE_TIME;
            nextIndex = 0;
        }

        /**
         * Changes the time for all {@link Element elements} in this context.
         *
         * @param time the time in milliseconds
         * @throws IllegalArgumentException if time is less than {@link Context#INIT_STATE_TIME}
         */
        public void setTime(int time) {
            if (time < -1) {
                throw new IllegalArgumentException("invalid time: " + time);
            }

            if (time == currentTime) {
                return;
            }

            if (time < currentTime) {
                resetTime();
            }

            ElementEvent event;
            while (nextIndex < events.size() && (event = events.get(nextIndex)).getTime() <= time) {
                currentTime = event.getTime();
                nextIndex += 1;
                event.fire();
            }
            currentTime = time;
        }

        public ObservableList<? extends Event> getEvents() {
            return FXCollections.unmodifiableObservableList(events);
        }
    }

    /**
     * Gets the {@link ElementFactory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if context is null
     */
    @Nonnull
    public static ElementFactory in(Context context) {
        return ElementFactory.getInstance(context);
    }

    @Nonnull
    private ElementFactory getFactory() {
        return this.factory;
    }

    /**
     * Adds an event for this {@link Element}.
     * If the time is negative, it will be corrected to 0 and a warning will be added to the event.
     *
     * @param state new state after this event
     * @param time the time of the event in milliseconds
     * @throws NullPointerException if state is null
     * @throws IllegalStateException if there is already another event after this one
     */
    public void addEvent(State state, int time) {
        List<String> warnings = new LinkedList<>();
        if (time < 0) {
            warnings.add("original time was " + time);
            time = 0;
        }
        getFactory().addEvent(this, Objects.requireNonNull(state), warnings, time);
    }

    @Override
    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph){
        this.graph=graph;
    }

    @Nonnull
    @Override
    public Shape createShape() {
        return getType().createShape();
    }

    @Nonnull
    @Override
    public Property<VisibleState> visibleStateProperty() {
        return getType().visibleStateProperty();
    }

    /**
     * Gets the {@link Node} this element is located at.
     *
     * @return the node
     */
    @Nonnull
    public Node getNode() {
        return node;
    }

    /**
     * Gets the property representing the {@link State} of this {@link Element}.<br>
     * The state will change, if {@link ElementFactory#setTime(int)} is called.
     *
     * @return the state property
     */
    @Nonnull
    public ReadOnlyProperty<State> stateProperty() {
        return stateProperty.getReadOnlyProperty();
    }

    /**
     * Gets the current {@link State} of this {@link Element}.<br> Note that the returned state is
     * immutable and therefore will not change if the state of this element changes. To track the
     * state of this element, use the {@link ReadOnlyProperty} returned by {@link #stateProperty()}.
     *
     * @return the current state
     */
    @Nonnull
    public State getState() {
        return stateProperty.getValue();
    }

    /**
     * Gets the {@link Type} of this {@link Element}.
     *
     * @return the type
     */
    @Nonnull
    public Type getType() {
        return type;
    }

    /**
     * Gets the switch this {@link Element} is part of.<br>There will only be a value present, if
     * the type of this element is {@link Type#WeichenPunkt}.
     *
     * @return the switch this element is part of
     * @throws IllegalStateException if this element doesn't have the WeichenPunkt type
     */
    @Nonnull
    public Switch getSwitch() {
        if (aSwitch == null) {
            throw new IllegalStateException();
        }
        return aSwitch;
    }
    public boolean setName(String newName){
        if(graph!=null){
            if(!Element.in(graph.getContext()).NameExists(newName)){
                this.name=newName;
                Element.in(graph.getContext()).elements.remove(newName);
                Element.in(graph.getContext()).elements.put(newName,this);
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Element{"
            + "name='" + name + '\''
            + ", state=" + stateProperty.getValue()
            + '}';
    }

    @ParametersAreNonnullByDefault
    private static class ElementEvent implements Event {

        @Nonnull
        private final Element element;
        private final int time;
        @Nonnull
        private final State state;
        @Nonnull
        private final ObservableList<String> warnings;

        private ElementEvent(Element element, int time, State state,
            ObservableList<String> warnings) {
            this.element = element;
            this.time = time;
            this.state = state;
            this.warnings = warnings;
        }

        @Nonnull
        public Element getElement() {
            return element;
        }

        @Override
        public int getTime() {
            return time;
        }

        @Nonnull
        @Override
        public String getDescription() {
            // TODO replace by something more human readable
            return toString();
        }

        @Nonnull
        @Override
        public ObservableList<String> getWarnings() {
            return warnings;
        }

        @Nonnull
        public State getState() {
            return state;
        }

        private void fire() {
            element.stateProperty.setValue(state);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }

            ElementEvent event = (ElementEvent) obj;

            if (!element.equals(event.element)) {
                return false;
            }
            if (time != event.time) {
                return false;
            }
            return state == event.state;
        }

        @Override
        public int hashCode() {
            int result = time;
            result = 31 * result + state.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ElementEvent{"
                + "time=" + time
                + ", element=" + getElement().getName()
                + ", state=" + state
                + '}';
        }
    }
}
