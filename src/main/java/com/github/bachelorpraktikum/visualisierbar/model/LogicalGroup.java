package com.github.bachelorpraktikum.visualisierbar.model;

import com.github.bachelorpraktikum.visualisierbar.model.Element.Type;
import com.github.bachelorpraktikum.visualisierbar.view.graph.GraphShape;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class LogicalGroup {

    @Nonnull
    private LinkedList<Element> elements;
    @Nonnull
    private String name;
    @Nonnull
    private final String oldName;
    @Nullable
    private Element belongsTo;
    @Nonnull
    private Kind type;
    @Nullable
    private String additional;


    public enum Kind {
        SIGNAL, SWITCH, LIMITER, DEFAULT
    }

    /**
     * Manages all instances of {@link LogicalGroup}.<br>
     * Ensures there is always only one instance of node per name per {@link Context}.
     */
    @ParametersAreNonnullByDefault
    public static final class GroupFactory{

        private static final Map<Context, WeakReference<GroupFactory>> instances = new WeakHashMap<>();
        private static final int INITIAL_NODES_CAPACITY = 32;


        @Nonnull
        private final Map<String, LogicalGroup> groups;
        @Nonnull
        private static GroupFactory getInstance(Context context){
            GroupFactory result = instances.computeIfAbsent(context, ctx -> {
                GroupFactory factory = new GroupFactory(ctx);
                ctx.addObject(factory);
                return new WeakReference<>(factory);
            }).get();

            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        }

        private GroupFactory(Context ctx) {
            this.groups = new LinkedHashMap<>(INITIAL_NODES_CAPACITY);
        }



        /**
         * Potentially creates a new instance of {@link LogicalGroup}.<br>
         * If a group with the same name already exists, an exception is thrown
         *
         * @param name the unique name of this group
         * @param kind the {@link Kind} of this Group
         * @param  elements the Elements this group will contain
         * @return the created group
         * @throws IllegalArgumentException if the name is already taken
         * @throws NullPointerException if any of the arguments is null
         */
        @Nonnull
        public LogicalGroup create(@Nonnull String name, @Nonnull Kind kind, @Nonnull LinkedList<Element> elements, @Nullable Element belongsTo){
            if(groups.containsKey(Objects.requireNonNull(name))){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, Objects.requireNonNull(kind), Objects.requireNonNull(elements), belongsTo);
                groups.put(g.getName(),g);
                return g;
            }
        }

        /**
         * Potentially creates a new instance of {@link LogicalGroup}.<br>
         * If a group with the same name already exists, an exception is thrown
         * @param name the unique name of this group
         * @param kind the {@link Kind} of this Group
         * @return the created group
         * @throws IllegalArgumentException if the name is already taken
         * @throws NullPointerException if any of the arguments is null
         */
        @Nonnull
        public LogicalGroup create(@Nonnull String name, @Nonnull Kind kind){
            if(groups.containsKey(Objects.requireNonNull(name))){throw new IllegalArgumentException("group already exist " + name);}
            else{
                LogicalGroup g= new  LogicalGroup(name, Objects.requireNonNull(kind));
                groups.put(g.getName(),g);
                return g;
            }
        }

        /**
         * Gets the instance with the given unique name.
         *
         * @param name the instance's name
         * @return the instance with this name
         * @throws NullPointerException if the name is null
         * @throws IllegalArgumentException if there is no object associated with the name
         */
        public LogicalGroup get(@Nonnull String name) {
            LogicalGroup group =groups.get(Objects.requireNonNull(name));
            if(group==null){
                throw new IllegalArgumentException("unknown group: " + name);
            }
            return  group;
        }
        /**
         * Gets all instances in this {@link Context}.
         *
         * @return all instances
         */
        @Nonnull
        public Collection<LogicalGroup> getAll() {
            return Collections.unmodifiableCollection(groups.values());
        }
        /**
         * Checks whether the given instance of {@link LogicalGroup} was created by this factory.
         *
         * @param LogicalGroup instance to check affiliation for
         * @return whether the given object was created by this factory
         */
        public boolean checkAffiliated(@Nonnull LogicalGroup LogicalGroup) {
            return groups.get(LogicalGroup.getName()) == LogicalGroup;
        }

        /**
         * Check whether a name is already taken
         * @param name the Name to check
         * @return  true if name is already taken, false otherwise
         * @throws NullPointerException is name is null
         */
        public boolean NameExists(@Nonnull String name) {
            LogicalGroup group = groups.get(Objects.requireNonNull(name));
            return group != null;
        }


    }
    /**
     * Gets the {@link GroupFactory} instance for the given {@link Context}.
     *
     * @param context the context
     * @return the factory
     * @throws NullPointerException if context is null
     */
    public static GroupFactory in(Context context) {
        return GroupFactory.getInstance(context);
    }


    private LogicalGroup(@Nonnull String name, @Nonnull Kind kind, @Nonnull LinkedList<Element> elements, @Nullable Element belongsTo)
        {this.name = name;
         this.type = kind;
         this.elements = new LinkedList<>();
         for(Element e : elements) {this.addElement(e);}                // Safe-Add Element
         this.belongsTo = belongsTo;
         this.oldName = name;
         this.additional = null;
        }

    private LogicalGroup(@Nonnull String name, @Nonnull Kind kind)
        {this.name = name;
         this.type = kind;
         this.elements = new LinkedList<>();
         this.oldName = name;
         this.additional = null;
        }

    /**
     * set the Additional-Parameter of this class to the given Value or to null if the given value is ""
     * @param additional new value of additional
     */
    public void setAdditional(@Nullable String additional) {
        this.additional = (additional == null || additional.length() == 0) ? null : additional;
    }

    @Nullable
    public String getAdditional()
        {return additional;}

    @Nonnull
    public String getName()
        {return name;}

    @Nonnull
    public Kind getKind()
        {return type;}

    /**
     * Adding an element to the group if it doesn't exist in the group yet and sets the elements logical group to this group
     * @param elem element to be added
     * @return true if successful, false if not
     */
    public boolean addElement(@Nonnull Element elem)
        {if(elements.contains(elem)) return false;
         elem.setLogicalGroup(this);
         elements.add(elem);
         return true;
        }

    /**
     * Adding an elemtn to the group under consideration of the group-specific Requirments
     * e.g. Signal contains 3 Magneten, 1 Hauptsignal, 1 Vorsignal and 1 Sichtbarkeitspunkt

     * @param elem element to be safe-added
     * @return null if successful, errormessage if not.
     */
    @Nullable
    public String safeAddElement(@Nonnull Element elem)
        {if(type == Kind.SIGNAL)
            {int countMagnet = elem.getType() == Type.Magnet ? 1 : 0;
             boolean hs = elem.getType() == Type.HauptSignal;
             boolean vs = elem.getType() == Type.VorSignal;
             boolean sp = elem.getType() == Type.SichtbarkeitsPunkt;

             if(!(elem.getType() == Type.Magnet ||
                  elem.getType() == Type.HauptSignal ||
                  elem.getType() == Type.VorSignal||
                  elem.getType() == Type.SichtbarkeitsPunkt))
                {return "Error: A Signal does not contain a " + elem.getReadableName();}

             for(Element e : elements)
                {if(e.getType() == Type.Magnet)
                    {countMagnet ++;
                     if(countMagnet > 3) {return "Error: LogicalGroup contains already 3 three Magnets!";}
                    }
                 if(e.getType() == Type.HauptSignal)
                    {if(hs) {return  "Error: LogicalGroup contains already a Hauptsignal!";}
                     hs = true;
                    }
                 if(e.getType() == Type.VorSignal)
                    {if(vs) {return  "Error: LogicalGroup contains already a Vorsignal!";}
                     vs = true;
                    }
                 if(e.getType() == Type.SichtbarkeitsPunkt)
                     {if(sp) {return  "Error: LogicalGroup contains already a Sichtbarkeitspunkt!";}
                      sp = true;
                     }
                }
             if(!addElement(elem))
                {return "Warning: This element already exists in LogicalGroup!";}
             return null;
            }
         if(type == Kind.LIMITER)
            {boolean vanz = elem.getType() == Type.GeschwindigkeitsVoranzeiger;
             boolean anz = elem.getType() == Type.GeschwindigkeitsAnzeiger;
             int magnet = elem.getType() == Type.Magnet ? 1 : 0;

             if(!(elem.getType() == Type.GeschwindigkeitsVoranzeiger||
                  elem.getType() == Type.GeschwindigkeitsAnzeiger ||
                  elem.getType() == Type.Magnet))
                {return "Error: A SpeedLimiter does not contain a " + elem.getReadableName();}

             for(Element e : elements)
                {if(e.getType() == Type.GeschwindigkeitsVoranzeiger)
                    {if(vanz) {return  "Error: LogicalGroup contains already a GeschwindigkeitsVoranzeiger!";}
                     vanz = true;
                    }
                 if(e.getType() == Type.GeschwindigkeitsAnzeiger)
                    {if(anz) {return  "Error: LogicalGroup contains already a GeschwindigkeitsAnzeiger!";}
                     anz = true;
                    }
                 if(e.getType() == Type.Magnet)
                    {magnet++;
                     if(magnet > 2) {return "Error LogicalGroup contains already 2 Magnets!";}
                    }
                }
             if(!addElement(elem))
                {return "Warning: This element already exists in LogicalGroup!";}
             return null;
            }
         if(type == Kind.SWITCH)
            {int wp = elem.getType() == Type.WeichenPunkt ? 1 : 0;

             if(elem.getType() != Type.WeichenPunkt)
                {return "Error: A Switch does not contain a " + elem.getReadableName();}

             for(Element e : elements)
                {if(e.getType() == Type.WeichenPunkt)
                    {wp++;
                     if(wp > 3) {return "Error: LogicalGroup contains already 3 Magnets!";}
                    }
                }
             if(!addElement(elem))
                {return "Warning: This element already exists in LogicalGroup!";}
             return null;
            }
         return "Error: Unknown group-type!";
        }

    public void setBelongsTo(@Nullable Element belongsTo)
        {this.belongsTo = belongsTo;}

    public Element getBelongsTo()
        {return this.belongsTo;}

    @Nonnull
    public LinkedList<Element> getElements(){
        return elements;
    }

    /**
     * Generates a row of Elements in the right order for ABS-Output.
     * E.G.: Signal: hs,m,m,vs,m,sp
     *       Switch: w,w,w
     *       Limiter: vanz,anz,m,m
     * Missing Elements will be "null"
     * @return the row of Elements
     */
    @Nonnull
    private String generateRowOfElements()
        {String rowOfElements = "";
         int counter = 0;
         if(type == Kind.SIGNAL)
            {String[] arrayElem = {"null", "null", "null", "null", "null", "null"};
             for(Element t : elements)
                {if(t.getType().equals(Type.HauptSignal))
                    {arrayElem[0] = t.higherName();}
                 if(t.getType().equals(Type.VorSignal))
                    {arrayElem[3] = t.higherName();}
                 if(t.getType().equals(Type.SichtbarkeitsPunkt))
                    {arrayElem[5] = t.higherName();}
                 if(t.getType().equals(Type.Magnet))
                    {if(arrayElem[1].equals("null"))
                        {arrayElem[1] = t.higherName();}
                     else if(arrayElem[2].equals("null"))
                        {arrayElem[2] = t.higherName();}
                     else
                        {arrayElem[4] = t.higherName();}
                    }
                }
             for(int i = 0; i <= 5; i++)
                {rowOfElements = rowOfElements.concat(arrayElem[i]).concat(", ");}
             return rowOfElements;
            }
         if(type == Kind.SWITCH)
            {for(Element t : elements)
                {rowOfElements = rowOfElements.concat(t.higherName().concat(", "));
                 counter ++;
                 if(counter > 3) {break;}
                }
             for(;counter<3;counter++) {rowOfElements = rowOfElements.concat("null, ");}
             return rowOfElements;
            }
         if(type == Kind.LIMITER)
            {String[] arrayElem = {"null", "null", "null", "null"};
             for(Element t : elements)
                {if(t.getType().equals(Type.GeschwindigkeitsVoranzeiger))
                    {arrayElem[1] = t.higherName();}
                 if(t.getType().equals(Type.GeschwindigkeitsAnzeiger))
                    {arrayElem[0] = t.higherName();}
                 if(t.getType().equals(Type.Magnet))
                    {if(arrayElem[2].equals("null"))
                        {arrayElem[2] = t.higherName();}
                     else
                        {arrayElem[3] = t.higherName();}
                    }
                }
             for(int i = 0; i <= 3; i++)
                {rowOfElements = rowOfElements.concat(arrayElem[i]).concat(", ");}
             return rowOfElements;
            }
         return "";
        }

    /**
     * Returns a String of the ABS representation of this LogicalGroup
     * @return the ABS-Code
     */
    @Nonnull
    public String toABS(String deltaContent)
        {String rowOfElements = generateRowOfElements();

         // Alle Elemente innerhalb einer logischen Gruppe werden erst nach dem erstellen der Logischen Gruppe hinzugefügt
         String addElem = "";
         for(Element t : elements)
            {addElem = addElem.concat(String.format("%s.addElement(%s);\n", t.getNode().higherName(), t.higherName()));}

         if(type == Kind.SIGNAL)
            {String belongOut = belongsTo == null ? "null /*TODO*/" : belongsTo.higherName();
             // Suche nach einem zfst in alter Datei - falls vorhanden
             String zfst = this.additional == null ? " null /*missing zfst*/" : " " + additional;
             if(deltaContent != null && this.additional == null)
                {//Pattern patternSignal = compile("(.*new SwWechselImpl\\()(.*?)(,\\p{Blank}*\"" + oldName + "\"\\);.*)");
                 Pattern patternSignal = compile("(.*new local SignalImpl\\(.*,\\p{Blank}*\"" + oldName + "\",\\p{Blank}*)(.*?)(\\);.*)");
                 try {Matcher matcherSignal = patternSignal.matcher(deltaContent);
                      matcherSignal.find();
                      zfst = matcherSignal.group(2);
                    }
                    catch(IllegalStateException ignored) {/*Falls Signal in alter Datei nicht gefunden werden konnte*/}
                }
             //Ende der Suche nach einem zfst

             String elemVorne = belongsTo != null ? belongsTo.getNode().higherName() : "/*missing Node*/";
             String elemHinten = belongsTo != null ? belongsTo.higherName() : "/*missing Element*/";

             addElem = addElem.concat(String.format("%s.addElement(%s);\n", elemVorne, elemHinten));

             return String.format("[HTTPName: \"%s\"]Signal %s = new local SignalImpl(%s\"%s\", %s);\n%s.setSignal(%s);\n%s\n",
                    name, name, rowOfElements, name, zfst, belongOut, name, addElem);
            }



         if(type == Kind.SWITCH)
            {Map<Edge, GraphShape<Edge>> edges = elements.get(0).getGraph().getEdges();
             String edge1 = "null";
             String edge2 = "null";

             // Suche die Kanten
             for(Map.Entry<Edge, GraphShape<Edge>> e : edges.entrySet())
                {Node node1 = e.getKey().getNode1();
                 Node node2 = e.getKey().getNode2();
                 if(node1.equals(elements.get(0).getNode()) && node2.equals(elements.get(1).getNode()) ||
                    node1.equals(elements.get(1).getNode()) && node2.equals(elements.get(0).getNode()) ||
                    node1.equals(elements.get(1).getNode()) && node2.equals(elements.get(2).getNode()) ||
                    node1.equals(elements.get(2).getNode()) && node2.equals(elements.get(1).getNode()) ||
                    node1.equals(elements.get(0).getNode()) && node2.equals(elements.get(2).getNode()) ||
                    node1.equals(elements.get(2).getNode()) && node2.equals(elements.get(0).getNode())
                   )
                        {if(edge1.equals("null")) {edge1 = e.getKey().higherName();}
                         else{edge2 = e.getKey().higherName(); break;}
                        }
                }

             //Suche true/false
             String bool = this.additional == null ? "false" : this.additional;
             if(deltaContent != null && this.additional == null)
                {Pattern patternSwitch = compile("(.*new local SwitchImpl\\(.*,\\p{Blank}*)(.*?)(,\\p{Blank}*\"" + oldName + "\"\\);.*)");
                 try {Matcher matcherSwitch = patternSwitch.matcher(deltaContent);
                      matcherSwitch.find();
                      bool = matcherSwitch.group(2);
                     }
                 catch(IllegalStateException ignored) {/*Falls Switch in alter Datei nicht gefunden werden konnte*/}
                }

             return String.format("[HTTPName: \"%s\"]Switch %s = new local SwitchImpl(%s%s, %s, %s, \"%s\");\n%s\n",
                    name, name, rowOfElements, edge1, edge2, bool, name, addElem);
            }



         if(type == Kind.LIMITER)
            {String belongOut = belongsTo == null ? "null /*TODO*/" : belongsTo.higherName();

             // Suche nach Speedlimit
             String limit = this.additional == null ? "40" : this.additional;
             if(deltaContent != null && this.additional == null)
                {Pattern patternLimit = compile("(.*new SpeedLimiterImpl\\(.*,\\p{Blank}*)(.*?)(,\\p{Blank}*\"" + oldName + "\"\\);.*)");
                    try {Matcher matcherLimit = patternLimit.matcher(deltaContent);
                        matcherLimit.find();
                        limit = matcherLimit.group(2);
                    }
                    catch(IllegalStateException ignored) {/*Falls Switch in alter Datei nicht gefunden werden konnte*/}
                }

             return String.format("[HTTPName: \"%s\"]SpeedLimiter %s = new SpeedLimiterImpl(%s%s, \"%s\");\n%s.setLogical(%s);\n%s\n",
                    name, name, rowOfElements, limit, name, belongOut, name, addElem);}

         return "// Type of logical group not supported";
        }

    /**
     * Removes an Element from this Group and sets its Logical Group to null
     * @param element the element to remove
     */
    public void removeElement(Element element){
        elements.remove(element);
        element.setLogicalGroup(null);
    }


}
