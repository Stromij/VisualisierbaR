package com.github.bachelorpraktikum.visualisierbar.model;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import sun.rmi.runtime.Log;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static java.util.regex.Pattern.compile;

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
    private  String oldName;
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
    @Nullable
    private Node direction;
    @Nullable
    private LogicalGroup logicalGroup;
    @Nullable
    private String absName;

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

             return (this == Type.GeschwindigkeitsAnzeiger) || (this == Type.GeschwindigkeitsVoranzeiger) || (this == Type.VorSignal) || (this == Type.HauptSignal);
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
        this.direction=null;
        this.logicalGroup =null;

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
            return element != null;
        }

        /**
         * Checks the availability of a ABS name
         * @param name the String to check
         * @return true, if an Node with this name exists, otherwise false
         */
        public boolean absNameExists(@Nonnull String name, @Nullable Element elem)
        {for(Map.Entry<String, Element> entry : elements.entrySet()) {
            if (Objects.equals(name, entry.getValue().getAbsName()) && !entry.getValue().equals(elem)) {
                return true;
            }
        }
            return false;
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
            LinkedList<Event>eList = new LinkedList<>();
            eList.addAll(element.getFactory().getEvents());
            events.removeAll(eList);
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


    @Nonnull
    public String getName() {
        return name;
    }


    @Nullable
    public Graph getGraph() {
        return graph;
    }

    public void setGraph(@Nullable Graph graph){
        this.graph=graph;
    }


    @Nullable
    public Node getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the element if allowed
     * @param direction the direction
     * @return true if it was a legal element, false if not
     */
    public boolean setDirection(@Nullable Node direction) {
        if(direction==null)
            this.direction=direction;
        for (Edge edge : this.getNode().getEdges()) {
            if ((edge.getNode1() == direction && edge.getOtherNode(edge.getNode1()) == this.getNode()) || (edge.getNode2() == direction && edge.getOtherNode(edge.getNode2()) == this.getNode())) {
                this.direction = direction;
                return true;
            }
        }
        return false;
    }

    @Nullable
    public LogicalGroup getLogicalGroup() {return logicalGroup;}

    public void setLogicalGroup(@Nullable LogicalGroup logicalGroup) {this.logicalGroup = logicalGroup;}

    /**
     * Set the absName to a given value, if there is no other Element in Graph with the same Name
     * @param newAbsName the new absName
     * @return false if something went wrong (linke double names), true if successful
     */
    public boolean setAbsName(@Nullable String newAbsName)
        {if(newAbsName == null) {absName = null; return true;}
         if(graph == null)  {this.absName = newAbsName; return true;}
         Boolean exit = Element.in(graph.getContext()).absNameExists(newAbsName, this);

         if(!exit)
            {this.absName = newAbsName;
                Element.in(graph.getContext()).elements.remove(name);
                Element.in(graph.getContext()).elements.put(name, this);
                return true;
            }
         return false;
        }

    @Nullable
    public String getAbsName() {return absName;}

    @Nonnull
    public String higherName() {return (absName == null) ? name : absName;}

    @Nullable
    public String getOldName() {return oldName;}

    public void setOldName(String oldName) {this.oldName = oldName;}

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
    /*
    public void setState(@Nonnull State state){
        stateProperty.setValue(state);
    }
    */



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


    /**
     * Returns a String of the ABS representation of this Element.
     *
     * @return the ABS-Code
     */
    @Nonnull
    public String toABS(@Nullable String deltaContent)
        {String highName = higherName();
         String addElem = "";
         if(logicalGroup == null)
             {addElem = String.format("%s.addElement(%s);\n", node.higherName(), highName);}
         Edge edgeDirection = null;
         for (Edge edge : this.getNode().getEdges()) {
            if ((edge.getNode1() == direction && edge.getOtherNode(edge.getNode1()) == this.getNode()) || (edge.getNode2() == direction && edge.getOtherNode(edge.getNode2()) == this.getNode())) {
                edgeDirection = edge;
            }
         }

         String edge = edgeDirection == null ? null : edgeDirection.higherName();

         if(getType() == Type.WeichenPunkt)
            {// [HTTPName: "w1_wa"]WeichenPunkt w1_wa = new local WeichenPunktImpl(n10, "w1_wa");
             return String.format("[HTTPName: \"%s\"]WeichenPunkt %s = new local WeichenPunktImpl(%s, \"%s\");\n%s",
                     highName, highName, node.higherName(), highName, addElem);
            }
         if(getType() == Type.HauptSignal)
            {// [HTTPName: "hs3"]HauptSignal hs3 = new local HauptSignalImpl(n34, e33, "hs3");
             return String.format("[HTTPName: \"%s\"]HauptSignal %s = new local HauptSignalImpl(%s, %s, \"%s\");\n%s",
                     highName, highName, node.higherName(), edge, highName, addElem);
                      //name, name, Nodename, Kantenname, addElem, name
            }
         if(getType() == Type.GefahrenPunkt)
            {// [HTTPName: "gp5"]GefahrenPunkt gp5 = new local GefahrenPunktImpl(e54, "gp5");
             // Achtung: Wenn Gefahrenpunkt zu einem Signal gehört, muss node.addElement später durchgeführt werden
             for(LogicalGroup g : LogicalGroup.in(graph.getContext()).getAll())
                {if(g.getBelongsTo() != null && g.getBelongsTo().equals(this))
                    {addElem = ""; break;}
                }

             return String.format("[HTTPName: \"%s\"]GefahrenPunkt %s = new local GefahrenPunktImpl(%s, \"%s\");\n%s",
                     highName, highName, edge, highName, addElem);
                      //name, name, Kantenname, name, addElem
            }
         if(getType() == Type.GeschwindigkeitsAnzeiger)
            {// [HTTPName: "vs2"]GeschwindigkeitsAnzeiger vs2 = new local GeschwindigkeitsAnzeigerImpl(e21, "vs2");
             return String.format("[HTTPName: \"%s\"]GeschwindigkeitsAnzeiger %s = new GeschwindigkeitsAnzeigerImpl(%s, \"%s\");\n%s",
                     highName, highName, edge, highName, addElem);
                //name, name, Kantenname, name, addElem
            }
         if(getType() == Type.GeschwindigkeitsVoranzeiger)
            {// [HTTPName: "vs2"]GeschwindigkeitsVoranzeiger vs2 = new local GeschwindigkeitsVoranzeiger(e21, "vs2");
                return String.format("[HTTPName: \"%s\"]GeschwindigkeitsVorAnzeiger %s = new GeschwindigkeitsVorAnzeigerImpl(%s, \"%s\");\n%s",
                        highName, highName, edge, highName, addElem);
                //name, name, Kantenname, name, addElem
            }
         if(getType() == Type.SwWechsel)
            {// [HTTPName: "ch5"]SwWechsel ch5 = new SwWechselImpl(zfst1, "ch5");
             // Suche in deltaContent nach dem Element um die zfst zu übernehmen.
             String zfst = "null /*missing zfst!*/";
             if(deltaContent != null)
                {Pattern patternSwWechsel = compile("(.*new SwWechselImpl\\()(.*?)(,\\p{Blank}*\"" + oldName + "\"\\);.*)");
                 try {Matcher matcherSwWechsel = patternSwWechsel.matcher(deltaContent);
                      matcherSwWechsel.find();
                      zfst = matcherSwWechsel.group(2);
                     }
                 catch(IllegalStateException ignored) {/*Falls SwWechsel in alter Datei nicht gefunden werden konnte*/}
                }
             return String.format("[HTTPName: \"%s\"]SwWechsel %s = new SwWechselImpl(%s, \"%s\");\n%s",
                     highName, highName, zfst, highName, addElem);
                      //name, name, zfst, name, addElem
            }
         if(getType() == Type.VorSignal)
            {// [HTTPName: "vs2"]VorSignal vs2 = new local VorSignalImpl(e21, "vs2");
             return String.format("[HTTPName: \"%s\"]VorSignal %s = new local VorSignalImpl(%s, \"%s\");\n%s",
                     highName, highName, edge, highName, addElem);
                      //name, name, Kantenname, name, addElem
            }
         if(getType() == Type.Magnet)
            {if(name.contains("PZBMagnetImpl"))
                {// [HTTPName: "m1"]Magnet m1 = new local PZBMagnetImpl(Mhz1000, e02, "m1");
                 // Default: 2000Mhz, erster Magnet in Signal: 1000Mhz, zweiter 500Mhz, dritter 2000Mhz
                 String mhz = "Mhz2000";
                 String local = "";
                 if(this.getLogicalGroup() != null && this.getLogicalGroup().getKind() == LogicalGroup.Kind.SIGNAL)
                    {int magnetCount = 0;
                     local = "local ";
                     String[] arrayOfMhz = {"Mhz1000", "Mhz500", "Mhz2000"};
                     for (Element e : getLogicalGroup().getElements()) {
                            if(e.equals(this))
                                {mhz = arrayOfMhz[magnetCount]; break;}
                            if(e.getType() == Type.Magnet && e.getName().contains("PZBMagnetImpl"))
                                {magnetCount++;}
                        }
                    }

                 return String.format("[HTTPName: \"%s\"]Magnet %s = new %sPZBMagnetImpl(%s, %s, \"%s\");\n%s",
                         highName, highName, local, mhz, edge, highName, addElem);
                      //name, name, MHz, Kantenname, name, addElem
                }
             if(name.contains("ContactMagnetImpl"))
                {//[HTTPName: "mv1"]ContactMagnet mv1 = new ContactMagnetImpl("mv1");
                 return String.format("[HTTPName: \"%s\"]ContactMagnet %s = new ContactMagnetImpl(\"%s\");\n%s",
                         highName, highName, highName, addElem);
                }

             //[HTTPName: "mv1"]Magnet mv1 = new MagnetImpl("mv1");
             return String.format("[HTTPName: \"%s\"]Magnet %s = new MagnetImpl(\"%s\");\n%s",
                        highName, highName, highName, addElem);
            }
         if(getType() == Type.SichtbarkeitsPunkt)
            {// [HTTPName: "ss2"]SichtbarkeitsPunkt ss2 = new local SichtbarkeitsPunktImpl(e15, "ss2");
             return String.format("[HTTPName: \"%s\"]SichtbarkeitsPunkt %s = new local SichtbarkeitsPunktImpl(%s, \"%s\");\n%s",
                     highName, highName, edge, highName, addElem);
                      //name, name, Kantenname, addElem
            }
         return "";
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

            return element.equals(event.element) && time == event.time && state == event.state;
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
